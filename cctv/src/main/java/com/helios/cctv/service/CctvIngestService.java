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
}