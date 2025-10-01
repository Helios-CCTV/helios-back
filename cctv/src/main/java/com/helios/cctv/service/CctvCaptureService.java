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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CctvCaptureService {

    @Qualifier("cctvExecutor")
    private final ThreadPoolTaskExecutor cctvExecutor;
    private final FrameCapture frameCapture;
    private final CctvProperties props;

    // 트리거ID -> 전역 시퀀스 카운터
    private final java.util.concurrent.ConcurrentMap<Long, java.util.concurrent.atomic.AtomicInteger> counters
            = new java.util.concurrent.ConcurrentHashMap<>();

    // ✅ 추가: 성공/실패 카운터
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);

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
        
        // ✅ 추가: 처리 통계 초기화 및 로깅
        successCount.set(0);
        failureCount.set(0);
        int totalCount = (int) list.stream()
                .filter(d -> d.getCctvurl() != null && !d.getCctvurl().isBlank())
                .count();
        
        log.info("CCTV 캡처 시작 - 총 {}개 처리 예정", totalCount);
        
        list.stream()
                .filter(d -> d.getCctvurl() != null && !d.getCctvurl().isBlank())
                .forEach(d -> cctvExecutor.execute(() -> {
                    try {
                        frameCapture.captureMany(d.getCctvurl(), d.getCctvname());
                        int success = successCount.incrementAndGet();
                        if (success % 100 == 0) {
                            log.info("진행상황 - 성공: {}, 실패: {}, 전체: {}", success, failureCount.get(), totalCount);
                        }
                    } catch (Exception e) {
                        int failure = failureCount.incrementAndGet();
                        log.error("CCTV 캡처 실패 - URL: {}, 이름: {}, 에러: {}", 
                                d.getCctvurl(), d.getCctvname(), e.getMessage());
                    }
                }));
        
        // ✅ 추가: ThreadPool 상태 모니터링
        log.info("ThreadPool 상태 - Active: {}, Queue: {}, Pool: {}", 
                cctvExecutor.getActiveCount(), 
                cctvExecutor.getThreadPoolExecutor().getQueue().size(),
                cctvExecutor.getPoolSize());
    }

    public void captureInOne(List<CctvApiDTO> list, long triggerId) {
        if (list == null || list.isEmpty()) {
            log.warn("empty CCTV list. triggerId={}", triggerId);
            return;
        }
        
        // ✅ 추가: 배치 처리로 변경하여 메모리 사용량 최적화
        int batchSize = 200; // 한번에 처리할 배치 크기
        var counter = counterOf(triggerId);
        
        log.info("CCTV 캡처 시작 - triggerId: {}, 총 {}개, 배치크기: {}", triggerId, list.size(), batchSize);
        
        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(i + batchSize, list.size());
            List<CctvApiDTO> batch = list.subList(i, end);
            
            log.info("배치 처리 중 - triggerId: {}, 배치: {}/{}, 범위: {}-{}", 
                    triggerId, (i/batchSize + 1), ((list.size()-1)/batchSize + 1), i+1, end);
            
            for (var d : batch) {
                if (d.getCctvurl() == null || d.getCctvurl().isBlank()) continue;
                int seq = counter.getAndIncrement();
                cctvExecutor.execute(() -> {
                    try {
                        frameCapture.captureManyInOne(d.getCctvurl(), d.getCctvname(), triggerId, seq);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        log.error("CCTV 캡처 실패 - triggerId: {}, seq: {}, URL: {}, 에러: {}", 
                                triggerId, seq, d.getCctvurl(), e.getMessage());
                    }
                });
            }
            
            // ✅ 추가: 배치간 잠시 대기 (시스템 부하 완화)
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        log.info("CCTV 캡처 완료 - triggerId: {}, 성공: {}, 실패: {}", 
                triggerId, successCount.get(), failureCount.get());
    }

    // ✅ 추가: 캡처 통계 조회 메서드
    public String getCaptureStats() {
        return String.format("성공: %d, 실패: %d, ThreadPool(Active/Queue/Pool): %d/%d/%d",
                successCount.get(), failureCount.get(),
                cctvExecutor.getActiveCount(),
                cctvExecutor.getThreadPoolExecutor().getQueue().size(),
                cctvExecutor.getPoolSize());
    }
}

