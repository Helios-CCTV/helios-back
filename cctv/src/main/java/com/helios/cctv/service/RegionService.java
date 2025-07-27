package com.helios.cctv.service;

import com.helios.cctv.dto.region.RegionDTO;
import com.helios.cctv.mapper.RegionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionService {
    private final RegionMapper regionMapper;

    public List<RegionDTO> getAllRegions() {
        List<RegionDTO> dto = regionMapper.getRegions();
        System.out.println(dto.stream().findFirst().get().getPolygon());
        return regionMapper.getRegions();
    }
}
