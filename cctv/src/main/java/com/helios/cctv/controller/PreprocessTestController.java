// com.helios.cctv.controller.PreprocessTestController.java
package com.helios.cctv.controller;

import com.helios.cctv.service.job.PreprocessEnqueue;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/preprocess")
@RequiredArgsConstructor
public class PreprocessTestController {
    private final PreprocessEnqueue enqueue;

    @PostMapping("/{cctvId}")
    public String enqueueJob(
            @PathVariable long cctvId,
            @RequestParam String hls,
            @RequestParam(defaultValue = "15") int sec
    ) {
        RecordId id = enqueue.enqueue(cctvId, hls, sec);
        return "Enqueued: " + id.getValue();
    }
}
