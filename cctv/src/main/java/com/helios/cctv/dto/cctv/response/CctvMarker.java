package com.helios.cctv.dto.cctv.response;

import lombok.Data;

@Data
public class CctvMarker {
    private String id;
    private String name;
    private float x;
    private float y;
    private String streamUrl;
}
