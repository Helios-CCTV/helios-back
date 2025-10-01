package com.helios.cctv.mapper;

import com.helios.cctv.dto.cluster.RegionClusterRow;
import com.helios.cctv.dto.region.RegionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RegionMapper {
    List<RegionDTO> getRegions();
    Integer findRegionIdByPoint(@Param("wkt") String wkt);
    List<RegionClusterRow> findRegionClustersInBounds(@Param("bounds5186") String boundsWkt);
}
