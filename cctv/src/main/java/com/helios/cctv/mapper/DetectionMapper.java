package com.helios.cctv.mapper;

import com.helios.cctv.dto.analyze.detection.Detection;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DetectionMapper {
    List<Detection> selectAll();
    List<Detection> selectDetection();
}
