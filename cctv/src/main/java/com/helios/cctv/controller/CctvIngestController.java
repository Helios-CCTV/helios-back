package com.helios.cctv.controller;

import com.helios.cctv.dto.ApiResponse;
import com.helios.cctv.dto.cctv.request.GetCctvRequest;
import com.helios.cctv.service.CctvIngestService;
import com.helios.cctv.service.CctvService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@CrossOrigin("http://localhost:4000/")
@RequestMapping("/cctv-ingest")
public class CctvIngestController {

    private final CctvIngestService cctvIngestService;

    @GetMapping("/save")
    public ResponseEntity<Void> getCctv(@ModelAttribute GetCctvRequest getCctvRequest) {
        cctvIngestService.ingest(getCctvRequest);
        return ResponseEntity.ok().build();
    }
}
