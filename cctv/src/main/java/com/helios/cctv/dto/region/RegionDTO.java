package com.helios.cctv.dto.region;

import lombok.Data;
import org.locationtech.jts.geom.Geometry;

import java.time.LocalDateTime;

@Data
public class RegionDTO {

    private Integer id;                   // 고유 ID
    private String admSectC;              // 행정구역 코드
    private String sggNm;                 // 행정구역 이름
    private Integer sggOid;               // SGG OID
    private String colAdmSe;              // 컬럼 코드
    private Geometry polygon;             // 경계 (Polygon 또는 MultiPolygon)
    private LocalDateTime createdAt;      // 생성 시각
    private LocalDateTime updatedAt;      // 업데이트 시각

    @Override
    public String toString() {
        return "RegionDTO{" +
                "id=" + id +
                ", admSectC='" + admSectC + '\'' +
                ", sggNm='" + sggNm + '\'' +
                ", sggOid=" + sggOid +
                ", colAdmSe='" + colAdmSe + '\'' +
                ", polygon=" + polygon +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
