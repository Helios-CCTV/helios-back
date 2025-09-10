package com.helios.cctv.service;

import com.helios.cctv.dto.cctv.CctvApiDTO;
import com.helios.cctv.dto.cctv.request.GetCctvRequest;
import com.helios.cctv.entity.cctv.Cctv;
import com.helios.cctv.repository.CctvRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CctvIngestService {
    private final CctvRepository cctvRepository;
    private final EntityManager em;
    private final CctvService apiService;

    // 전체를 호출하는 엔트리포인트 (사용자는 이 메서드만 호출)
    public void ingest(GetCctvRequest req) {
        log.info("CCTV 데이터 수집 시작 - 요청: {}", req);
        
        List<CctvApiDTO> dtos = apiService.getCctvApi(req);
        if (dtos == null || dtos.isEmpty()) {
            log.warn("조회된 CCTV 데이터가 없습니다.");
            return;
        }
        
        log.info("CCTV 데이터 조회 완료 - 총 {}개", dtos.size());
        
        // ✅ 추가: 유효한 URL을 가진 CCTV 개수 확인
        long validUrlCount = dtos.stream()
                .filter(d -> d.getCctvurl() != null && !d.getCctvurl().isBlank())
                .count();
        
        log.info("유효한 CCTV URL 개수: {} / {}", validUrlCount, dtos.size());
        
        saveAllInBatches(dtos, 500); // 4천+건이면 500씩 자동 루프
        
        log.info("CCTV 데이터 저장 완료");
    }

    // URL만 업데이트하는 로직 type 1 : https, type 2 : http
    public void updateCctvUrls(GetCctvRequest req, boolean isHttps) {
        log.info("CCTV URL 업데이트 시작 - 요청: {}", req);

        
        List<CctvApiDTO> dtos = apiService.getCctvApi(req);
        if (dtos == null || dtos.isEmpty()) {
            log.warn("조회된 CCTV 데이터가 없습니다.");
            return;
        }
        
        log.info("CCTV 데이터 조회 완료 - 총 {}개", dtos.size());
        
        updateUrlsInBatches(dtos, 500, isHttps);
        
        log.info("CCTV URL 업데이트 완료");
    }

    // 새로 추가: URL 업데이트를 배치로 처리
    public void updateUrlsInBatches(List<CctvApiDTO> dtos, int batchSize, boolean isHttps) {
        int totalUpdated = 0;
        int totalNotFound = 0;
        
        for (int i = 0; i < dtos.size(); i += batchSize) {
            int end = Math.min(i + batchSize, dtos.size());
            List<CctvApiDTO> batch = dtos.subList(i, end);
            
            var result = updateUrlBatch(batch, isHttps);
            totalUpdated += result[0];
            totalNotFound += result[1];
            
            log.info("배치 처리 완료 [{}/{}] - 업데이트: {}, 미발견: {}", 
                    end, dtos.size(), result[0], result[1]);
        }
        
        log.info("URL 업데이트 총 결과 - 업데이트: {}, 미발견: {}", totalUpdated, totalNotFound);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int[] updateUrlBatch(List<CctvApiDTO> batch, boolean isHttps) {
        int updated = 0;
        int notFound = 0;

        // 배치로 모아서 한 번에 저장
        List<Cctv> toSave = new ArrayList<>();

        for (CctvApiDTO dto : batch) {
            // 1) 필수 데이터 검증
            String name = dto.getCctvname();
            if (name == null || name.isBlank()) continue;

            String newUrl = trimToNull(dto.getCctvurl()); // DTO는 항상 cctvurl만 의미있음
            if (newUrl == null) continue;

            // coordx 파싱 (DB 컬럼 scale=6에 맞춰 정규화 권장)
            BigDecimal coordx = parseDecimal(dto.getCoordx()); // parseDecimal에서 scale 맞추면 더 안전
            if (coordx == null) continue;

            // 2) 기존 CCTV 조회 (name + coordx)
            List<Cctv> existingList = cctvRepository.findByCctvnameAndCoordx(name, coordx);

            if (existingList.isEmpty()) {
                notFound++;
                log.debug("기존 CCTV 미발견 - name: {}, coordx: {}", name, coordx);
                continue;
            }

            // 3) 여러 개 발견 시 모두 처리하되, 값이 바뀔 때만 저장/카운트
            for (Cctv existing : existingList) {
                if (isHttps) {
                    String old = existing.getCctvurl();
                    if (!newUrl.equals(old)) {
                        existing.setCctvurl(newUrl);
                        toSave.add(existing);
                        updated++;
                        log.debug("CCTV URL(HTTPS) 업데이트 - id: {}, name: {}, coordx: {}, {} -> {}",
                                existing.getId(), name, coordx, safe(old), newUrl);
                    } else {
                        log.debug("변경 없음(HTTPS) - id: {}, name: {}, coordx: {}", existing.getId(), name, coordx);
                    }
                } else {
                    String oldPre = existing.getCctvurl_pre();
                    if (!newUrl.equals(oldPre)) {
                        existing.setCctvurl_pre(newUrl);
                        toSave.add(existing);
                        updated++;
                        log.debug("CCTV URL(HTTP-pre) 업데이트 - id: {}, name: {}, coordx: {}, {} -> {}",
                                existing.getId(), name, coordx, safe(oldPre), newUrl);
                    } else {
                        log.debug("변경 없음(HTTP-pre) - id: {}, name: {}, coordx: {}", existing.getId(), name, coordx);
                    }
                }
            }
        }

        // 4) 배치 저장 (라운드트립 최소화)
        if (!toSave.isEmpty()) {
            cctvRepository.saveAll(toSave);
        }
        cctvRepository.flush();
        em.clear();

        return new int[]{updated, notFound};
    }

    // 배치마다 별도 트랜잭션(부분성공/복구 용이)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSlice(List<Cctv> slice) {
        cctvRepository.saveAll(slice);
        cctvRepository.flush();
        em.clear();
    }

    // 500개씩 끊어 저장
    public void saveAllInBatches(List<CctvApiDTO> dtos, int batchSize) {
        for (int i = 0; i < dtos.size(); i += batchSize) {
            int end = Math.min(i + batchSize, dtos.size());
            List<Cctv> sliceEntities = dtos.subList(i, end).stream()
                    .map(this::toEntity)
                    .toList();
            saveSlice(sliceEntities); // 내부에서 @Transactional
        }
    }

    // DTO → Entity 매핑 (엔티티가 Double 기준)
    private Cctv toEntity(CctvApiDTO d) {
        Cctv e = new Cctv();
        e.setRoadsectionid(nullIfBlank(d.getRoadsectionid()));
        e.setFilecreatetime(nullIfBlank(d.getFilecreatetime()));
        e.setCctvtype(nullIfBlank(d.getCctvtype()));
        e.setCctvurl(nullIfBlank(d.getCctvurl()));
        e.setCctvresolution(nullIfBlank(d.getCctvresolution()));
        e.setCoordx(parseDecimal(d.getCoordx())); // 문자열 → BigDecimal(소수6자리)
        e.setCoordy(parseDecimal(d.getCoordy()));
        e.setCctvformat(nullIfBlank(d.getCctvformat()));
        e.setCctvname(nullIfBlank(d.getCctvname()));
        e.setCctvurl_pre(nullIfBlank(d.getCctvurl_pre()));
        return e;
    }

    private String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private BigDecimal parseDecimal(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        try {
            return new BigDecimal(s).setScale(6, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String safe(String s) {
        return s == null ? "(null)" : s;
    }

}