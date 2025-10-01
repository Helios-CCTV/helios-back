package com.helios.cctv.service;

import com.helios.cctv.dto.ApiResponse;
import com.helios.cctv.dto.report.ReportDTO;
import com.helios.cctv.entity.Report;
import com.helios.cctv.repository.ReportRepository;
import com.helios.cctv.repository.projection.ReportListItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private static final long MAX_BYTES = 20L * 1024 * 1024; // 20MB

    private final ReportRepository reportRepository;

    //신고 저장
    @Transactional
    public ApiResponse<String> save(ReportDTO dto) {
        try {
            Report report = new Report();

            // 1) 필수/기본값 매핑
            Report.DamageType type = Report.DamageType.valueOf(dto.getDamageType().toUpperCase());
            report.setDamageType(type);
            report.setLocation(dto.getLocation());
            report.setSeverity(dto.getSeverity());
            report.setDescription(dto.getDescription());
            report.setName(dto.getName());
            report.setContact(dto.getContact());
            report.setReportDate(dto.getReportDate() != null ? dto.getReportDate() : java.time.LocalDateTime.now());

            // 2) 파일 처리 (MultipartFile -> byte[])
            if (dto.getPhoto() != null && !dto.getPhoto().isEmpty()) {
                if (dto.getPhoto().getSize() > MAX_BYTES) {
                    return ApiResponse.fail("첨부 파일은 최대 20MB까지 가능합니다.", 400);
                }
                byte[] bytes = dto.getPhoto().getBytes();
                report.setPhotoBlob(bytes);
                report.setPhotoFilename(
                        dto.getPhoto().getOriginalFilename() != null ? dto.getPhoto().getOriginalFilename() : "photo"
                );
                // photo_mime 컬럼을 안 쓰는 구조라면 생략. 쓰려면 엔티티/테이블에 추가하고 set 하세요.
                // report.setPhotoMime(dto.getPhoto().getContentType());
            }

            Report saved = reportRepository.save(report);
            if (saved.getId() != null) {
                return ApiResponse.ok("신고가 정상 처리되었습니다.", 200);
            }
            return ApiResponse.fail("저장 중 알 수 없는 오류", 500);

        } catch (IllegalArgumentException e) {
            // damageType 매핑 실패 등
            return ApiResponse.fail("파손 유형이 올바르지 않습니다.", 400);
        } catch (IOException e) {
            return ApiResponse.fail("첨부 파일 처리에 실패했습니다.", 500);
        } catch (Exception e) {
            return ApiResponse.fail("저장 실패", 500);
        }
    }

    //신고 리스트 조회
    public ApiResponse<List<ReportListItem>> getAll() {
        try{
            List<ReportListItem> list = reportRepository.findAllByOrderByReportDateDesc();
            return ApiResponse.ok(list,200);
        } catch (Exception e){
            return ApiResponse.fail("조회 실패", 500);
        }
    }

    //신고 상세 조회
    public ApiResponse<Report> getById(Long id) {
        try {
            Report report = reportRepository.findById(id).orElse(null);
            return ApiResponse.ok(report,200);
        } catch (Exception e){
            return ApiResponse.fail("조회 실패", 500);
        }
    }

}

