package com.helios.cctv.service;

import com.helios.cctv.dto.cctv.CctvMini;
import com.helios.cctv.dto.cctv.response.JobResponse;
import com.helios.cctv.entity.cctv.Cctv;
import com.helios.cctv.repository.CctvRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import org.springframework.data.redis.core.SessionCallback;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class PreprocessJobService {
    private final CctvRepository cctvRepository;
    private final StringRedisTemplate redis;

    private static final String STREAM = "stream:preprocess";
    private static final String GROUP  = "workers";
    private final ThreadPoolTaskExecutor enqueueExecutor;

    @PostConstruct
    void initGroup() {
        try {
            // 그룹이 없으면 생성 (이미 있으면 에러 → 무시)
            redis.opsForStream().createGroup(STREAM, ReadOffset.latest(), GROUP);
        } catch (Exception ignore) {}
    }

    //전처리 작업 전송
    public String enqueuePreprocess(Long cctvId, Integer sec) {
        Cctv cctv = cctvRepository.findById(cctvId)
                .orElseThrow(() -> new IllegalArgumentException("cctv not found: " + cctvId));

        String jobId = UUID.randomUUID().toString();

        // 상태 해시 초기화
        String jobKey = "job:" + jobId;
        Map<String, String> init = new HashMap<>();
        init.put("state", "QUEUED");
        init.put("progress", "0");
        init.put("message", "queued");
        init.put("createdAt", String.valueOf(System.currentTimeMillis()));
        redis.opsForHash().putAll(jobKey, init);

        // 최신 업데이트 인덱스
        redis.opsForZSet().add("z:job:updated", jobId, System.currentTimeMillis());

        // 스트림 이벤트 생성
        MapRecord<String, Object, Object> record = StreamRecords
                .newRecord()
                .in(STREAM)
                .ofMap(Map.of(
                        "jobId", jobId,
                        "cctvId", String.valueOf(cctvId),
                        "hls", cctv.getCctvurl(),
                        "sec", String.valueOf(sec == null ? 0 : sec),
                        "createdAt", String.valueOf(System.currentTimeMillis())
                ));

        redis.opsForStream().add(record);

        return jobId;
    }

    //작업 조회
    public JobResponse getStatus(String jobId) {
        String key = "job:" + jobId;
        Map<Object, Object> m = redis.opsForHash().entries(key);
        if (m.isEmpty()) throw new NoSuchElementException("job not found");

        return new JobResponse(
                jobId,
                (String)m.getOrDefault("state", "UNKNOWN"),
                Integer.valueOf((String)m.getOrDefault("progress", "0")),
                (String)m.getOrDefault("message", "")
        );
    }

    //일괄 전처리
    public int allEnqueuePreprocess() {
        final String roadType = "EX";
        final int pageSize = 200;    // DB 페이징
        final int step     = 20;     // 한 배치에 넣을 작업 수 (10~50 권장)

        Pageable page = PageRequest.of(0, pageSize, Sort.by("id").ascending());
        int total = 0;

        for (;;) {
            Slice<CctvMini> slice = cctvRepository.findByRoadTypeMini(roadType, page);
            if (slice.isEmpty()) break;

            var list = slice.getContent();
            total += list.size();

            // step 크기로 쪼개서 각각을 executor로 보냄(동시 실행)
            List<CompletableFuture<Void>> futures = new java.util.ArrayList<>();
            final long now = System.currentTimeMillis();

            for (int i = 0; i < list.size(); i += step) {
                var part = list.subList(i, Math.min(i + step, list.size()));
                futures.add(CompletableFuture.runAsync(() -> {
                    // 단건 XADD 반복(파이프라인 X) — 네트워크 여건에선 이게 더 안전
                    for (CctvMini row : part) {
                        var rec = StreamRecords.newRecord()
                                .in(STREAM)
                                .ofStrings(Map.of(
                                        "cctvId",   String.valueOf(row.getId()),
                                        "hls",      row.getCctvurl(),
                                        "sec",      "15",
                                        "attempt",  "1",
                                        "enqueuedAt", String.valueOf(now)
                                ));

                        // 필요하면 MAXLEN≈으로 무한증가 방지 (Spring Data 3.5)
                        // redis.opsForStream().add(rec, XAddOptions.maxlen(10000).approximateTrimming(true));
                        redis.opsForStream().add(rec); // 기본
                    }
                }, enqueueExecutor));
            }

            // 현재 페이지의 모든 배치가 끝날 때까지 대기(다음 페이지로 넘어가기 전에만 기다림)
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

            if (!slice.hasNext()) break;
            page = slice.nextPageable();
        }

        return total;
    }


}
