package com.helios.cctv.controller;

import com.helios.cctv.dto.region.RegionDTO;
import com.helios.cctv.dto.region.response.RegionResponse;
import com.helios.cctv.service.RegionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@CrossOrigin("http://localhost:4000/")
@RequestMapping("/region")
public class RegionController {

    private final RegionService regionService;

    @GetMapping("/getAll")
    public List<RegionResponse> getRegions() {
        List<RegionDTO> dtoList = regionService.getAllRegions();
        return dtoList.stream()
                .map(RegionResponse::from)
                .collect(Collectors.toList());
    }
}
