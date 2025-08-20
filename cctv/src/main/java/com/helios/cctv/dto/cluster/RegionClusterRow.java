// package는 네 프로젝트 구조에 맞춰서
package com.helios.cctv.dto.cluster;

import lombok.Data;

@Data
public class RegionClusterRow {
    private Integer regionId; // r.id AS regionId
    private String  name;     // r.sgg_nm AS name
    private Double  x;        // ST_X(...) AS x  (EPSG:5186)
    private Double  y;        // ST_Y(...) AS y  (EPSG:5186)
    private Integer count;    // CCTV 개수
    private String  polygonWkt; // ★ 추가
}
