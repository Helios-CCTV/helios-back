package com.helios.cctv.config;

import com.helios.cctv.properties.CctvProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class CctvExecutorConfig {

    @Bean(name = "cctvExecutor")
    public ThreadPoolTaskExecutor cctvExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(4);              // 필요시 프로퍼티로 뺄 것
        ex.setMaxPoolSize(4);
        ex.setQueueCapacity(1000);
        ex.setThreadNamePrefix("cctv-");
        ex.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        ex.initialize();
        return ex;
    }
}

