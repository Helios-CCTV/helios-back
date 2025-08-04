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

    private static final java.util.concurrent.atomic.AtomicLong TRIGGER_SEQ = new java.util.concurrent.atomic.AtomicLong(0);

    // ex) POST /admin/cctv/capture  (필요하면 바디/쿼리로 필터 전달)
    @PostMapping("/capture")
    public String capture(@RequestBody GetCctvRequest getCctvRequest) {
        List<CctvApiDTO> list = cctvService.getCctvApi(getCctvRequest);
        cctvCaptureService.captureAll(list);
        return "started";
    }

    @PostMapping("/capture-in-one")
    public String captureInOne(@RequestBody GetCctvRequest getCctvRequest, @RequestParam(required = false) Long triggerId) {
        long tid = (triggerId != null) ? triggerId : TRIGGER_SEQ.incrementAndGet();
        var list = cctvService.getCctvApi(getCctvRequest);
        cctvCaptureService.captureInOne(list, tid);
        return "started triggerId=" + tid + ", size=" + (list == null ? 0 : list.size());
    }
}

