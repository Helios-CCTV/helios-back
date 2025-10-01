// record 사용 (JDK 17+)
package com.helios.cctv.dto.cluster;

public record ClusterItem(
        Integer regionId,
        String  name,
        double  lat,   // WGS84
        double  lng,   // WGS84
        int     count,
        String  polygon // ★ GeoJSON (WGS84)
) {}
