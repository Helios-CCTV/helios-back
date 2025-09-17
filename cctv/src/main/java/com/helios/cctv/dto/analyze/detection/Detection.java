package com.helios.cctv.dto.analyze.detection;

import lombok.Data;
import java.util.List;

@Data
public class Detection {
    private int id;                 // cctvId (cctvs.id)
    private String cctvName;        // cctvs.cctvname
    private String cctvUrl;
    private long analyzeId;         // analyzes.id  ← 추가
    private String date;            // analyzes.analyzed_date → String으로
    private List<String> detections; // detections.damage_type 목록
}