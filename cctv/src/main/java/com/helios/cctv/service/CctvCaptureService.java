package com.helios.cctv.service;

import com.helios.cctv.dto.cctv.CctvApiDTO;
import com.helios.cctv.properties.CctvProperties;
import com.helios.cctv.util.FrameCapture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CctvCaptureService {

    private final FrameCapture frameCapture;
    private final CctvProperties props;
    private final Executor cctvExecutor;

    public void captureAll(List<CctvApiDTO> list) {
        if (list == null || list.isEmpty()) {
            log.warn("CCTV 목록이 비어 있습니다.");
            return;
        }
        list.stream()
                .filter(d -> d.getCctvurl() != null && !d.getCctvurl().isBlank())
                .forEach(d -> cctvExecutor.execute(() ->
                        frameCapture.captureMany(d.getCctvurl(), d.getCctvname())));
    }
}

