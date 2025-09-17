package com.helios.cctv.entity;

import com.helios.cctv.dto.report.ReportDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Entity
@Table(name="road_damage_reports")
@Getter
@Setter
public class Report {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)   // ENUM을 문자열로 매핑
    @Column(name = "damage_type", nullable = false)
    private DamageType damageType;

    private String location;

    private int severity;

    private String description;

    @Column(name = "photo_url")
    private String photoUrl;

    private String name;

    private String contact;

    @Column(name = "report_date")
    private java.time.LocalDateTime reportDate;

    @Column(name = "is_checked")
    private Boolean isChecked = false;

    @Getter
    public enum DamageType {
        POTHOLE("포트홀"),
        CRACK("균열"),
        SETTLEMENT("침하"),
        SINKHOLE("함몰"),
        CONSTRUCTION_CRACK("시공균열"),
        ALLIGATOR_CRACK("거북등"),
        SHOVING("쇼빙"),
        ETC("기타");

        private final String label;

        DamageType(String label) {
            this.label = label;
        }
    }

    // 한글 라벨 → Enum 변환
    public static DamageType fromLabel(String label) {
        return Arrays.stream(DamageType.values())
                .filter(d -> d.label.equals(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid damage type: " + label));
    }

    public static Report transform(ReportDTO dto){
        Report report = new Report();
        DamageType type = DamageType.valueOf(dto.getDamageType());
        report.setDamageType(type);
        report.setLocation(dto.getLocation());
        report.setSeverity(dto.getSeverity());
        report.setDescription(dto.getDescription());
        report.setPhotoUrl("dto.getPhoto()");
        report.setName(dto.getName());
        report.setContact(dto.getContact());
        report.setReportDate(dto.getReportDate());
        return report;
    }
}


