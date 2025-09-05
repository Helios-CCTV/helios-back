package com.helios.cctv.service.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.helios.cctv.dto.cctv.CctvApiDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service@RequiredArgsConstructor
public class JobProducer {
    private final StringRedisTemplate stringRedisTemplate;

    public void enqueueJobs(List<CctvApiDTO> allData) throws JsonProcessingException {
        List<List<CctvApiDTO>> batches = Lists.partition(allData, 500);
        for(List<CctvApiDTO> batch : batches){
            Map<String, String> payload = Map.of(
                    "batch", new ObjectMapper().writeValueAsString(batch)
            );
            stringRedisTemplate.opsForStream().add(
                    StreamRecords.newRecord()
                            .in("stream:cctv:jobs")
                            .ofMap(payload)
            );
        }
    }
}
