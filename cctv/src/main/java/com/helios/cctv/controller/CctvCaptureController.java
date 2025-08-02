package com.helios.cctv.controller;

import com.helios.cctv.dto.cctv.CctvApiDTO;
import com.helios.cctv.dto.cctv.request.GetCctvRequest;
import com.helios.cctv.service.CctvCaptureService;
import com.helios.cctv.service.CctvService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/cctv")
@RequiredArgsConstructor
public class CctvCaptureController {

    private final CctvService cctvService; // 이미 구현한 서비스
    private final CctvCaptureService cctvCaptureService;

    // ex) POST /admin/cctv/capture  (필요하면 바디/쿼리로 필터 전달)
    @PostMapping("/capture")
    public String capture(@RequestBody GetCctvRequest getCctvRequest) {
        List<CctvApiDTO> list = cctvService.getCctvApi(getCctvRequest);
        cctvCaptureService.captureAll(list);
        return "started";
    }
}

