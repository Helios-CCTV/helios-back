package com.helios.cctv.controller;

import com.helios.cctv.dto.ApiResponse;
import com.helios.cctv.dto.analyze.detection.Detection;
import com.helios.cctv.service.AnalyzeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/analyze")
@RequiredArgsConstructor
public class AnalyzeController {


    private final AnalyzeService analyzeService;

    @GetMapping("/date")
    public Void analyzeDate(int date){
        analyzeService.analyzeDate(date);
        return null;
    }

    @GetMapping("/getDetection")
    public ApiResponse<List<Detection>> getDetection(){
        return analyzeService.getDetection();
    }
}
