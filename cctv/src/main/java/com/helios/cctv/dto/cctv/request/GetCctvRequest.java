package com.helios.cctv.dto.cctv.request;

import lombok.Data;

@Data
public class GetCctvRequest {
    private float minX;
    private float maxX;
    private float minY;
    private float maxY;
}
