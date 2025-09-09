package com.helios.cctv.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class EnqExecConfig {
    @Bean("enqueueExecutor")
    public ThreadPoolTaskExecutor enqueueExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(3);      // 동시 배치 작업 수 (2~4 추천)
        ex.setMaxPoolSize(3);
        ex.setQueueCapacity(100);   // 대기열
        ex.setThreadNamePrefix("bulk-enq-");
        ex.initialize();
        return ex;
    }
}

