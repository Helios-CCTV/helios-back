package com.helios.cctv.dto.cctv;

import lombok.Data;

@Data
public class CctvApiDTO {
    private String roadsectionid;    // 도로 구간 ID
    private String filecreatetime;   // 파일 생성 시간 (YYYYMMDDHH24MISS)
    private String cctvtype;         // CCTV 유형
    private String cctvurl;          // CCTV 영상 주소
    private String cctvresolution;   // CCTV 해상도
    private String coordx;           // 경도 좌표
    private String coordy;           // 위도 좌표
    private String cctvformat;       // CCTV 형식
    private String cctvname;         // CCTV 설치 장소명
    private String cctvurl_pre;
}
