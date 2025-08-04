package com.helios.cctv.service;

import com.helios.cctv.dto.cctv.CctvApiDTO;
import com.helios.cctv.properties.CctvProperties;
import com.helios.cctv.util.FrameCapture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CctvCaptureService {

    @Qualifier("cctvExecutor")
    private final ThreadPoolTaskExecutor cctvExecutor; // 2번째 파라미터였던 그 타입
    private final FrameCapture frameCapture;
    private final CctvProperties props;
    //private final Executor cctvExecutor;

    // 트리거ID -> 전역 시퀀스 카운터
    private final java.util.concurrent.ConcurrentMap<Long, java.util.concurrent.atomic.AtomicInteger> counters
            = new java.util.concurrent.ConcurrentHashMap<>();

    // ✅ 변경: 디스크를 스캔해서 초기값을 max+1로 설정
    private java.util.concurrent.atomic.AtomicInteger counterOf(long triggerId) {
        return counters.computeIfAbsent(triggerId, id -> new java.util.concurrent.atomic.AtomicInteger(initSeqFromDisk(id)));
    }

    // ✅ 추가: capture/{triggerId} 디렉토리에서 "{triggerId}_\\d{5}.jpg" 중 최대값을 찾아 +1 리턴
    private int initSeqFromDisk(long triggerId) {
        var baseDir = java.nio.file.Paths.get(props.getCapture().getOutputDir(), String.valueOf(triggerId));
        if (!java.nio.file.Files.isDirectory(baseDir)) {
            return 1;
        }
        int max = 0;
        var pattern = java.util.regex.Pattern.compile("^" + triggerId + "_(\\d{5})\\.jpg$", java.util.regex.Pattern.CASE_INSENSITIVE);

        try (var stream = java.nio.file.Files.list(baseDir)) {
            for (String name : (Iterable<String>) stream.map(p -> p.getFileName().toString())::iterator) {
                var m = pattern.matcher(name);
                if (m.matches()) {
                    int seq = Integer.parseInt(m.group(1));
                    if (seq > max) max = seq;
                }
            }
        } catch (java.io.IOException e) {
            log.warn("initSeqFromDisk scan failed. triggerId={}, dir={}, err={}", triggerId, baseDir, e.toString());
        }
        return max + 1; // ✅ 기존 최대 번호 다음부터 시작
    }

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

    public void captureInOne(List<CctvApiDTO> list, long triggerId) {
        if (list == null || list.isEmpty()) {
            log.warn("empty CCTV list. triggerId={}", triggerId);
            return;
        }
        var counter = counterOf(triggerId); // ✅ 트리거 전역 시퀀스
        for (var d : list) {
            if (d.getCctvurl() == null || d.getCctvurl().isBlank()) continue;
            int seq = counter.getAndIncrement(); // ✅ 트리거 내 유니크 번호
            cctvExecutor.execute(() ->
                    frameCapture.captureManyInOne(d.getCctvurl(), d.getCctvname(), triggerId, seq)
            );
        }
    }
}

