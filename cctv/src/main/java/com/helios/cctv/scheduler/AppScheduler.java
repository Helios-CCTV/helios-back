package com.helios.cctv.scheduler;

import com.helios.cctv.dto.cctv.request.GetCctvRequest;
import com.helios.cctv.service.CctvIngestService;
import com.helios.cctv.service.PreprocessJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppScheduler {

    private final PreprocessJobService preprocessJobService;
    private final CctvIngestService cctvIngestService;

    @Scheduled(cron = "0 30 6 * * *", zone = "Asia/Seoul")
    public void dailyPreprocess() {
        preprocessJobService.allEnqueuePreprocess();
    }

    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Seoul")
    public void dailyUpdateHLS(){
        GetCctvRequest request = new GetCctvRequest();
        request.setMinX(125.189f);
        request.setMaxX(129.584f);
        request.setMinY(33.569f);
        request.setMaxY(38.686f);
        request.setRoadType("ex");   // ex = 고속도로
        request.setCctvType("4");

        cctvIngestService.updateCctvUrls(request,true);
        cctvIngestService.updateCctvUrls(request,false);
    }

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void runAtEveryMinute() {
        System.out.println("Schedule test by 1 minute");
    }



}
