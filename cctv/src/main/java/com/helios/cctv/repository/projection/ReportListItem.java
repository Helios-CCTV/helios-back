package com.helios.cctv.repository.projection;

import com.helios.cctv.entity.Report;

public interface ReportListItem {
    Long getId();
    Report.DamageType getDamageType();
    String getLocation();
    Integer getSeverity();
    Boolean getIsChecked();
}
