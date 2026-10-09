package com.placement.sms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "placement_criteria")
public class PlacementCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false, unique = true, length = 100)
    private String companyName;

    @Column(name = "min_cgpa", nullable = false, precision = 4, scale = 2)
    private BigDecimal minCgpa;

    @Column(name = "max_active_backlogs", nullable = false)
    private int maxActiveBacklogs;

    public PlacementCriteria() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        id = value;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String value) {
        companyName = value;
    }

    public BigDecimal getMinCgpa() {
        return minCgpa;
    }

    public void setMinCgpa(BigDecimal value) {
        minCgpa = value;
    }

    public int getMaxActiveBacklogs() {
        return maxActiveBacklogs;
    }

    public void setMaxActiveBacklogs(int value) {
        maxActiveBacklogs = value;
    }
}
