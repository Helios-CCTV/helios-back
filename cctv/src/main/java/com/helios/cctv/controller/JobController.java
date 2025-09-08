package com.helios.cctv.controller;

import com.helios.cctv.dto.cctv.request.PreprocessRequest;
import com.helios.cctv.dto.cctv.response.JobResponse;
import com.helios.cctv.service.PreprocessJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final PreprocessJobService service;

    @PostMapping("/preprocess")
    public Map<String, String> create(@RequestBody PreprocessRequest req) {
        String jobId = service.enqueuePreprocess(req.cctvId(), req.sec());
        return Map.of("jobId", jobId);
    }

    @PostMapping("/preprocess/all")
    public Map<String, Object> enqueueAll() {
        int n = service.allEnqueuePreprocess();
        return Map.of("queued", n);
    }

    @GetMapping("/status/{jobId}")
    public JobResponse status(@PathVariable String jobId) {
        return service.getStatus(jobId);
    }
}
