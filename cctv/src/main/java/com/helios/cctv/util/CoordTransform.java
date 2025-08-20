package com.helios.cctv.util;

import org.locationtech.proj4j.*;
import org.springframework.stereotype.Component;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.locationtech.proj4j.*;

@Component
public class CoordTransform {
    private final CoordinateTransform to5186;
    private final CoordinateTransform to4326;

    public CoordTransform() {
        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem wgs84   = crsFactory.createFromName("EPSG:4326");
        CoordinateReferenceSystem epsg5186= crsFactory.createFromName("EPSG:5186");
        CoordinateTransformFactory f = new CoordinateTransformFactory();
        to5186 = f.createTransform(wgs84, epsg5186);
        to4326 = f.createTransform(epsg5186, wgs84);
    }

    public static record Bounds(double minX, double minY, double maxX, double maxY) {}
    public static record LngLat(double lng, double lat) {}

    public Bounds to5186(double minLng, double minLat, double maxLng, double maxLat) {
        ProjCoordinate a = new ProjCoordinate(minLng, minLat);
        ProjCoordinate b = new ProjCoordinate(maxLng, maxLat);
        ProjCoordinate da = new ProjCoordinate();
        ProjCoordinate db = new ProjCoordinate();
        to5186.transform(a, da);
        to5186.transform(b, db);
        return new Bounds(Math.min(da.x, db.x), Math.min(da.y, db.y),
                Math.max(da.x, db.x), Math.max(da.y, db.y));
    }

    public LngLat to4326(double x5186, double y5186) {
        ProjCoordinate src = new ProjCoordinate(x5186, y5186);
        ProjCoordinate dst = new ProjCoordinate();
        to4326.transform(src, dst);
        return new LngLat(dst.x, dst.y);
    }


    public Geometry simplify5186(Geometry g5186, double toleranceMeters) {
        if (toleranceMeters <= 0) return g5186;
        return TopologyPreservingSimplifier.simplify(g5186, toleranceMeters);
    }

    /** 5186 -> 4326 변환 (in-place 변환) */
    public Geometry geomTo4326(Geometry g5186) {
        g5186.apply(new CoordinateSequenceFilter() {
            @Override public void filter(CoordinateSequence seq, int i) {
                ProjCoordinate src = new ProjCoordinate(seq.getX(i), seq.getY(i));
                ProjCoordinate dst = new ProjCoordinate();
                to4326.transform(src, dst);
                seq.setOrdinate(i, 0, dst.x);
                seq.setOrdinate(i, 1, dst.y);
            }
            @Override public boolean isDone() { return false; }
            @Override public boolean isGeometryChanged() { return true; }
        });
        g5186.setSRID(4326);
        return g5186;
    }

    public String wkt5186ToGeoJson4326(String wkt5186, double simplifyMeters) {
        try {
            Geometry g = new WKTReader().read(wkt5186);
            g.setSRID(5186);
            Geometry simplified = simplify5186(g, simplifyMeters); // 수준에 따라 0 or 값
            Geometry g4326 = geomTo4326(simplified);
            return new GeoJsonWriter().write(g4326);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert polygon WKT to GeoJSON", e);
        }
    }

    /** 줌 레벨 -> 단순화 허용오차(미터) 간단 매핑 */
    public double simplifyToleranceMeters(int level) {
        if (level <= 8)  return 0;     // 상세
        if (level <= 10) return 5;     // 살짝
        if (level <= 12) return 20;    // 중간
        if (level <= 14) return 50;
        return 100;                    // 많이 축소
    }
}

