package com.helios.cctv.dto.region;

import lombok.Data;
import org.locationtech.jts.geom.Geometry;

import java.time.LocalDateTime;

@Data
public class RegionDTO {

    private Integer id;                   // 고유 ID
    private String adm_sect_c;              // 행정구역 코드
    private String sgg_nm;                 // 행정구역 이름
    private Integer sgg_oid;               // SGG OID
    private String col_adm_se;              // 컬럼 코드
    private Geometry polygon;             // 경계 (Polygon 또는 MultiPolygon)
    private LocalDateTime created_at;      // 생성 시각
    private LocalDateTime updated_at;      // 업데이트 시각

    @Override
    public String toString() {
        return "RegionDTO{" +
                "id=" + id +
                ", admSectC='" + adm_sect_c + '\'' +
                ", sggNm='" + sgg_nm + '\'' +
                ", sggOid=" + sgg_oid +
                ", colAdmSe='" + col_adm_se + '\'' +
                ", polygon=" + polygon +
                ", createdAt=" + created_at +
                ", updatedAt=" + updated_at +
                '}';
    }
}
