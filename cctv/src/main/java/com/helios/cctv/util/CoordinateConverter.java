package com.helios.cctv.util;

import org.locationtech.proj4j.*;

public class CoordinateConverter {

    private static final CoordinateTransform transform;

    static {
        CRSFactory factory = new CRSFactory();
        CoordinateReferenceSystem wgs84 = factory.createFromName("EPSG:4326");
        CoordinateReferenceSystem koreaTm = factory.createFromName("EPSG:5186");

        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        transform = ctFactory.createTransform(wgs84, koreaTm);
    }

    public static ProjCoordinate to5186(double lon, double lat) {//정밀도를 위해 Double 선택
        ProjCoordinate src = new ProjCoordinate(lon, lat);
        ProjCoordinate dst = new ProjCoordinate();
        transform.transform(src, dst);
        return dst;
    }
}
