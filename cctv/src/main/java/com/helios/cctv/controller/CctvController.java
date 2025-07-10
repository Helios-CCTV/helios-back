package com.helios.cctv.controller;

import com.helios.cctv.dto.cctv.request.GetCctvRequest;
import com.helios.cctv.service.CctvService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/cctv")
public class CctvController {

    private final CctvService cctvService;

    @GetMapping
    public ResponseEntity<String> getCctv(@RequestBody GetCctvRequest getCctvRequest) {

        return ResponseEntity.ok(cctvService.getCctv(getCctvRequest));
    }
}
