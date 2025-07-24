package com.helios.cctv.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(Geometry.class)
public class GeometryTypeHandler extends BaseTypeHandler<Geometry> {

    private final WKBReader reader = new WKBReader();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Geometry parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setBytes(i, new WKBWriter().write(parameter));
    }

    @Override
    public Geometry getNullableResult(ResultSet rs, String columnName) throws SQLException {
        byte[] bytes = rs.getBytes(columnName);
        if (bytes == null) return null;
        try {
            return reader.read(bytes);
        } catch (ParseException e) {
            throw new SQLException("Failed to parse geometry", e);
        }
    }

    @Override
    public Geometry getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        byte[] bytes = rs.getBytes(columnIndex);
        if (bytes == null) return null;
        try {
            return reader.read(bytes);
        } catch (ParseException e) {
            throw new SQLException("Failed to parse geometry", e);
        }
    }

    @Override
    public Geometry getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        byte[] bytes = cs.getBytes(columnIndex);
        if (bytes == null) return null;
        try {
            return reader.read(bytes);
        } catch (ParseException e) {
            throw new SQLException("Failed to parse geometry", e);
        }
    }
}
