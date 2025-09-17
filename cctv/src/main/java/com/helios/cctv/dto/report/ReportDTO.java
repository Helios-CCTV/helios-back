package com.helios.cctv.dto.report;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class ReportDTO {
    private Long id;
    private String damageType;
    private String location;
    private int severity;
    private String description;
    private MultipartFile photo;
    private String name;
    private String contact;
    private LocalDateTime reportDate;
    private Boolean isChecked;
}
