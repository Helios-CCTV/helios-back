package com.helios.cctv.scheduler;

import com.helios.cctv.service.PreprocessJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppScheduler {

    private final PreprocessJobService preprocessJobService;

    @Scheduled(cron = "0 30 6 * * *", zone = "Asia/Seoul")
    public void dailyPreprocess() {
        preprocessJobService.allEnqueuePreprocess();
    }

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void runAtEveryMinute() {
        System.out.println("Schedule test by 1 minute");
    }



}
