package com.helios.cctv.config;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    RedissonClient redissonClient(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port,
            @Value("${spring.data.redis.password:}") String password) {

        var cfg = new org.redisson.config.Config();
        var s = cfg.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setPassword(password == null || password.isBlank() ? null : password)
                // 연결/명령 대기 시간
                .setConnectTimeout(15000)     // 8s → 15s
                .setTimeout(60000)            // 20s → 60s
                .setRetryAttempts(5)          // 3 → 5
                .setRetryInterval(2000)       // 1s → 2s
                // 커넥션 풀(기본값이 작으면 대기 발생)
                .setConnectionPoolSize(64)
                .setConnectionMinimumIdleSize(16)
                // 헬스체크
                .setPingConnectionInterval(30000)
                .setKeepAlive(true)
                .setTcpNoDelay(true);
        if (!password.isBlank()) s.setPassword(password);
        return org.redisson.Redisson.create(cfg);
    }

    @Bean
    org.redisson.spring.data.connection.RedissonConnectionFactory redissonConnectionFactory(RedissonClient c) {
        return new org.redisson.spring.data.connection.RedissonConnectionFactory(c);
    }
}

