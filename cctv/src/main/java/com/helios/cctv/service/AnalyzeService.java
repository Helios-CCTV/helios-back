package com.helios.cctv.service;

import com.helios.cctv.dto.ApiResponse;
import com.helios.cctv.dto.analyze.detection.Detection;
import com.helios.cctv.mapper.DetectionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyzeService {

    final private DetectionMapper detectionMapper;

    public Void analyzeDate(int date){
        return null;
    }

    //분석 조회
    public ApiResponse<List<Detection>> getAnalyze(){
        try{
            List<Detection> result = detectionMapper.selectAll();
            return ApiResponse.ok(result,200);
        } catch (Exception e) {
            return ApiResponse.fail("파손 조회 실패", 500);
        }
    }

    //파손 조회
    public ApiResponse<List<Detection>> getDetection(){
        try{
            List<Detection> result = detectionMapper.selectDetection();
            return ApiResponse.ok(result,200);
        } catch (Exception e) {
            return ApiResponse.fail("파손 조회 실패", 500);
        }
    }
    
    
}
