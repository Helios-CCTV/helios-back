package com.helios.cctv.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component // 또는 @EnableConfigurationProperties(CctvProperties.class) 사용
@ConfigurationProperties(prefix = "cctv")
public class CctvProperties {
    private Capture capture = new Capture();

    @Data
    public static class Capture {
        private String outputDir;
        private int width;
        private int height;
        private int secondsToCapture;
        private int fps;
        private String ffmpegPath;
        private int maxParallel;
    }
}

