package com.helios.cctv.controller;

import com.helios.cctv.dto.region.RegionDTO;
import com.helios.cctv.service.RegionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin("http://localhost:4000/")
@RequestMapping("/region")
public class RegionController {

    private final RegionService regionService;

    @GetMapping("/get")
    public List<RegionDTO> getRegions() {
        return regionService.getAllRegions();
    }
}
