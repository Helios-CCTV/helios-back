package com.helios.cctv.controller;

import com.helios.cctv.dto.cctv.request.GetCctvRequest;
import com.helios.cctv.service.CctvIngestService;
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

    @GetMapping("/updateHLS")
    public ResponseEntity<Void> updateHLS(@ModelAttribute GetCctvRequest getCctvRequest) {
        cctvIngestService.updateCctvUrls(getCctvRequest,true);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/updateHLS-pre")
    public ResponseEntity<Void> updateHLSPreprocess(@ModelAttribute GetCctvRequest getCctvRequest) {
        cctvIngestService.updateCctvUrls(getCctvRequest,false);
        return ResponseEntity.ok().build();
    }
}
