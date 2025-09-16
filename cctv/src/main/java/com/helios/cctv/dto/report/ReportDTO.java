package com.helios.cctv.dto.report;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportDTO {
    private Long id;
    private String damageType;
    private String location;
    private int severity;
    private String description;
    private String photoUrl;
    private String name;
    private String contact;
    private LocalDateTime reportDate;
    private Boolean isChecked;
}
