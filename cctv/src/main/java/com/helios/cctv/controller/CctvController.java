package com.helios.cctv.controller;

import com.helios.cctv.dto.cctv.ApiResponse;
import com.helios.cctv.dto.cctv.request.GetCctvRequest;
import com.helios.cctv.service.CctvService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@CrossOrigin("http://localhost:4000/")
@RequestMapping("/cctv")
public class CctvController {

    private final CctvService cctvService;

    @GetMapping("/view")
    public ResponseEntity<ApiResponse<String>> getCctv(@ModelAttribute GetCctvRequest getCctvRequest) {
        ApiResponse<String> apiResponse = cctvService.getCctv(getCctvRequest);
        if (apiResponse.isSuccess()) {
            return ResponseEntity.ok(apiResponse);
        } else {
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }
}
