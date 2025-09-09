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
                .setDatabase(0)                 // 꼭 명시: RedisInsight와 같은 DB 보기
                .setConnectTimeout(15000)
                .setTimeout(60000)
                .setRetryAttempts(5)
                .setRetryInterval(2000)
                .setConnectionPoolSize(64)
                .setConnectionMinimumIdleSize(16)
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

