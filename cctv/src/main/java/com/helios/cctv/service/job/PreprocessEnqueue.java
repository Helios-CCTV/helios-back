// com.helios.cctv.service.job.PreprocessEnqueue.java
package com.helios.cctv.service.job;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PreprocessEnqueue {
    private final StringRedisTemplate redis;

    public RecordId enqueue(long cctvId, String hlsUrl, int seconds) {
        var fields = Map.of(
                "cctvId", String.valueOf(cctvId),
                "hls", hlsUrl,
                "sec", String.valueOf(seconds),
                "attempt", "0",
                "enqueuedAt", String.valueOf(Instant.now().toEpochMilli())
        );

        // MAXLEN≈10000으로 스트림 무한증가 방지(대략치)
        return redis.opsForStream().add(
                StreamRecords.newRecord()
                        .in("stream:preprocess")
                        .ofMap(fields)
                        .withId(RecordId.autoGenerate())
        );
    }

    public boolean tryEnqueueOnce(long cctvId, String hls, int sec) {
        var lockKey = "lock:preprocess:" + cctvId;
        Boolean ok = redis.opsForValue().setIfAbsent(lockKey, "1", java.time.Duration.ofMinutes(5)); //TTL
        if (Boolean.TRUE.equals(ok)) {
            enqueue(cctvId, hls, sec);
            return true;
        }
        return false; // 이미 대기 중
    }
}
