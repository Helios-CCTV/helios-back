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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.redis.core.SessionCallback;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PreprocessJobService {
    private final CctvRepository cctvRepository;
    private final StringRedisTemplate redis;

    private static final String STREAM = "s:preprocess";
    private static final String GROUP  = "workers";

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
        final int pageSize = 500;
        Pageable page = PageRequest.of(0, pageSize, Sort.by("id").ascending());
        int total = 0;

        for (;;) {
            Slice<CctvMini> slice = cctvRepository.findByRoadTypeMini(roadType, page);
            if (slice.isEmpty()) break;

            long now = System.currentTimeMillis();

            SessionCallback<Object> cb = new SessionCallback<>() {
                @Override @SuppressWarnings("unchecked")
                public Object execute(RedisOperations operations) {
                    RedisOperations<String,String> ops = (RedisOperations<String,String>) operations;

                    for (CctvMini row : slice.getContent()) {
                        String jobId = UUID.randomUUID().toString();
                        String jobKey = "job:" + jobId;

                        ops.opsForHash().putAll(jobKey, Map.of(
                                "state","QUEUED","progress","0","message","queued","createdAt",String.valueOf(now)
                        ));
                        ops.expire(jobKey, Duration.ofDays(7)); // 선택

                        ops.opsForZSet().add("z:job:updated", jobId, now);

                        MapRecord<String, String, String> rec = StreamRecords
                                .newRecord()
                                .in(STREAM)
                                .ofStrings(Map.of(
                                        "jobId", jobId,
                                        "cctvId", String.valueOf(row.getId()),
                                        "hls", row.getCctvurl(),
                                        "sec", "0",
                                        "createdAt", String.valueOf(now)
                                ));
                        ops.opsForStream().add(rec);
                    }
                    return null;
                }
            };

            redis.executePipelined(cb);

            total += slice.getNumberOfElements();
            if (!slice.hasNext()) break;
            page = slice.nextPageable();
        }
        return total;
    }


}
