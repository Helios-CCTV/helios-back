package com.helios.cctv.config;

import com.helios.cctv.properties.CctvProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class CctvExecutorConfig {

    @Bean("cctvExecutor")
    public Executor cctvExecutor(CctvProperties props) {
        var cap = props.getCapture();
        var ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(cap.getMaxParallel());
        ex.setMaxPoolSize(cap.getMaxParallel());
        ex.setQueueCapacity(100);
        ex.setThreadNamePrefix("cctv-");
        ex.initialize();
        return ex;
    }
}

