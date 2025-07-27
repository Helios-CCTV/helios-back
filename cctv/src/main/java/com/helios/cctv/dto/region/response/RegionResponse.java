package com.helios.cctv.dto.region.response;

import com.helios.cctv.dto.region.RegionDTO;
import lombok.Data;
import org.locationtech.jts.io.WKTWriter;

import java.time.LocalDateTime;

@Data
public class RegionResponse {
    private Integer id;
    private String adm_sect_c;
    private String sgg_nm;
    private Integer sgg_oid;
    private String col_adm_se;
    private String polygon; // ← Geometry 대신 WKT
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public static RegionResponse from(RegionDTO dto) {
        WKTWriter writer = new WKTWriter();
        String wkt = writer.write(dto.getPolygon());

        RegionResponse res = new RegionResponse();
        res.id = dto.getId();
        res.adm_sect_c = dto.getAdm_sect_c();
        res.sgg_nm = dto.getSgg_nm();
        res.sgg_oid = dto.getSgg_oid();
        res.col_adm_se = dto.getCol_adm_se();
        res.polygon = wkt;
        res.created_at = dto.getCreated_at();
        res.updated_at = dto.getUpdated_at();
        return res;
    }
}
