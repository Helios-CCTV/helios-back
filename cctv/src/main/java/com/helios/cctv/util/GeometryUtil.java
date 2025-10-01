package com.helios.cctv.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class GeometryUtil {
    private static final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

    public static Point toPoint(String coordx, String coordy) {
        return factory.createPoint(new Coordinate(
                Double.parseDouble(coordx),
                Double.parseDouble(coordy)
        ));
    }
}
