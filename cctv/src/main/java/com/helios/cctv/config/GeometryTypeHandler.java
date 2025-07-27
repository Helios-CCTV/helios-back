package com.helios.cctv.config;

import org.apache.ibatis.type.*;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(Geometry.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class GeometryTypeHandler implements TypeHandler<Geometry> {

    private final WKBReader reader = new WKBReader();
    private final WKBWriter writer = new WKBWriter();


    public void setParameter(PreparedStatement ps, int i, Geometry parameter, JdbcType jdbcType) throws SQLException {
        ps.setBytes(i, writer.write(parameter));
    }

    @Override
    public Geometry getResult(ResultSet rs, String columnName) throws SQLException {
        byte[] bytes = rs.getBytes(columnName);
        if (bytes == null) return null;

        try {
            return reader.read(bytes);
        } catch (ParseException e) {
            throw new SQLException("Failed to parse WKB geometry", e);  // 감싸서 던지기
        }
    }

    @Override
    public Geometry getResult(ResultSet rs, int columnIndex) throws SQLException {
        byte[] bytes = rs.getBytes(columnIndex);
        if (bytes == null) return null;
        try {
            return reader.read(bytes);
        } catch (ParseException e) {
            throw new SQLException("Failed to parse WKB geometry", e);
        }
    }

    @Override
    public Geometry getResult(CallableStatement cs, int columnIndex) throws SQLException {
        byte[] bytes = cs.getBytes(columnIndex);
        if (bytes == null) return null;
        try {
            return reader.read(bytes);
        } catch (ParseException e) {
            throw new SQLException("Failed to parse WKB geometry", e);
        }
    }
}
