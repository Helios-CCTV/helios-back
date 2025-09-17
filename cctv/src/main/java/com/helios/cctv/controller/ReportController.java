package com.helios.cctv.controller;

import com.helios.cctv.dto.ApiResponse;
import com.helios.cctv.dto.report.ReportDTO;
import com.helios.cctv.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {


    private final ReportService reportService;

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> save(@ModelAttribute ReportDTO report) {
        ApiResponse<String> result = reportService.save(report);
        return result.isSuccess() ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }
}
