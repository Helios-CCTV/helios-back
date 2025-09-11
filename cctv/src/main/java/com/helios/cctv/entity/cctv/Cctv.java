package com.helios.cctv.entity.cctv;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cctvs")
@Getter
@Setter
public class Cctv {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roadsectionid;
    private String filecreatetime;
    private String cctvtype;

    @Column(columnDefinition = "TEXT")
    private String cctvurl;

    private String cctvresolution;

    @Column(precision = 10, scale = 6)
    private BigDecimal coordx;

    @Column(precision = 10, scale = 6)
    private BigDecimal coordy;

    private String cctvformat;
    @Column(name = "road_type", length = 10)
    private String roadType;
    private String cctvname;

    @Column(columnDefinition = "TEXT")
    private String cctvurl_pre;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}


