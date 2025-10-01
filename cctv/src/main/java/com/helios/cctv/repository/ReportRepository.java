package com.helios.cctv.repository;

import com.helios.cctv.entity.Report;
import com.helios.cctv.repository.projection.ReportListItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<ReportListItem> findAllByOrderByReportDateDesc();
}
