package com.placement.sms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class AcademicRecord {

    @Column(name = "cgpa", nullable = false, precision = 4, scale = 2)
    private BigDecimal cgpa;

    @Column(name = "active_backlogs", nullable = false)
    private int activeBacklogs;

    public AcademicRecord() {
    }

    public AcademicRecord(BigDecimal cgpaValue, int backlogsValue) {
        cgpa = cgpaValue;
        activeBacklogs = backlogsValue;
    }

    public BigDecimal getCgpa() {
        return cgpa;
    }

    public void setCgpa(BigDecimal value) {
        cgpa = value;
    }

    public int getActiveBacklogs() {
        return activeBacklogs;
    }

    public void setActiveBacklogs(int value) {
        activeBacklogs = value;
    }
}
