package com.helios.cctv.util;

import com.helios.cctv.properties.CctvProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FrameCapture {

    private final CctvProperties props;
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public void captureMany(String hlsUrl, String cameraName) {
        var cap = props.getCapture();

        try {
            // 출력 폴더 준비
            String ts = LocalDateTime.now().format(TS_FMT);
            String safeName = cameraName == null ? "unknown"
                    : cameraName.replaceAll("[^ㄱ-ㅎ가-힣a-zA-Z0-9_\\-\\.\\[\\]\\(\\) ]", "_");
            File dir = new File(cap.getOutputDir(), ts + "_" + safeName);
            Files.createDirectories(dir.toPath());

            String outPattern = new File(dir, "%05d.jpg").getAbsolutePath();

            List<String> cmd = new ArrayList<>();
            cmd.add(cap.getFfmpegPath());       // 예: "ffmpeg" 또는 "C:/ffmpeg/bin/ffmpeg.exe"
            cmd.add("-y");
            cmd.add("-hide_banner");
            cmd.add("-loglevel"); cmd.add("error");
            // 필요하면 Referer/Cookie 등 헤더 추가 가능
            // cmd.add("-headers"); cmd.add("Referer: https://...\\r\\nUser-Agent: Mozilla\\r\\n");

            cmd.add("-i"); cmd.add(hlsUrl);     // HLS(m3u8) URL
            cmd.add("-t"); cmd.add(String.valueOf(cap.getSecondsToCapture()));
            cmd.add("-vf");
            cmd.add(String.format(
                    "fps=%d,scale=%d:%d:force_original_aspect_ratio=decrease,pad=%d:%d:(ow-iw)/2:(oh-ih)/2",
                    cap.getFps(), cap.getWidth(), cap.getHeight(), cap.getWidth(), cap.getHeight()
            ));
            cmd.add(outPattern);

            Process proc = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start();

            try (var is = proc.getInputStream()) { is.transferTo(System.out); }
            int exit = proc.waitFor();
            if (exit != 0) {
                log.warn("FFmpeg 종료코드 {} (URL: {})", exit, hlsUrl);
            } else {
                log.info("캡처 완료: URL={}, 폴더={}", hlsUrl, dir.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("캡처 실패 ({}): {}", cameraName, e.getMessage(), e);
        }
    }
}

