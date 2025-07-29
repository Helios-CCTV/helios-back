package com.helios.cctv.dto.cctv;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

@Data
public class CctvDTO {
    private int id;
    private String location;
    private double latitude;
    private double longitude;
    private Point point; // PostGIS 또는 Geometry 라이브러리를 쓰면 적절히 타입 수정
    private String status;
    private int regionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
