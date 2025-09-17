package com.helios.cctv.controller;

import com.helios.cctv.dto.ApiResponse;
import com.helios.cctv.dto.report.ReportDTO;
import com.helios.cctv.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {


    private final ReportService reportService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<String>> save(@ModelAttribute ReportDTO report) {
        ApiResponse<String> result = reportService.save(report);
        if(result.isSuccess()){
            return ResponseEntity.ok(result);
        } else{
            return ResponseEntity.badRequest().body(result);
        }
    }
}
