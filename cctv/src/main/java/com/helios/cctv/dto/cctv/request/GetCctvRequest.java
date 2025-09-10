package com.helios.cctv.dto.cctv.request;

import lombok.Data;

@Data
public class GetCctvRequest {
    private float minX;
    private float maxX;
    private float minY;
    private float maxY;
    private int level;
    private String roadType;
    private String cctvType;
}
//type 1 : 실시간 스트리밍 http
//type 2 : 동영상 http
//type 3 : 정지 영상
//type 4 : 실시간 스트리밍 https
//type 5 : 동영상 https
//its 국도, ex 고속도로
