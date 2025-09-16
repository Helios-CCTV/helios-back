package com.helios.cctv.service;

import com.helios.cctv.dto.ApiResponse;
import com.helios.cctv.dto.report.ReportDTO;
import com.helios.cctv.entity.Report;
import com.helios.cctv.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;

    public ApiResponse<String> save(ReportDTO reportDTO) {
        try{
            Report report = Report.transform(reportDTO);
            Report entity = reportRepository.save(report);
            if(entity.getId() != null) {
                return ApiResponse.ok("신고가 정상 처리되었습니다.",200);
            } else {
                return ApiResponse.ok("오류발생", 500);
            }
        } catch (Exception e){
            return ApiResponse.fail("저장 실패", 500);
        }

    }
}
