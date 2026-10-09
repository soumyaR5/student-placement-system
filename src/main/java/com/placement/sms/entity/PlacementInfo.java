package com.placement.sms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;

@Embeddable
public class PlacementInfo {

    @Enumerated(EnumType.STRING)
    @Column(name = "placement_status", nullable = false, length = 20)
    private PlacementStatus status = PlacementStatus.NOT_PLACED;

    @Column(name = "company_placed", length = 100)
    private String companyPlaced;

    @Column(name = "package_lpa", precision = 8, scale = 2)
    private BigDecimal packageLpa;

    public PlacementInfo() {
    }

    public PlacementInfo(PlacementStatus statusValue, String companyValue, BigDecimal packageValue) {
        status = statusValue;
        companyPlaced = companyValue;
        packageLpa = packageValue;
    }

    public PlacementStatus getStatus() {
        return status;
    }

    public void setStatus(PlacementStatus value) {
        status = value;
    }

    public String getCompanyPlaced() {
        return companyPlaced;
    }

    public void setCompanyPlaced(String value) {
        companyPlaced = value;
    }

    public BigDecimal getPackageLpa() {
        return packageLpa;
    }

    public void setPackageLpa(BigDecimal value) {
        packageLpa = value;
    }
}
