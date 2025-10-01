package com.helios.cctv.service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService { //Java 코드에서 Redis 명령어를 실행할 수 있게 해줌
    private final StringRedisTemplate stringRedisTemplate;

    public void save(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public String find(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }
}
