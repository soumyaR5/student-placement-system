package com.placement.sms.dto;

import com.placement.sms.entity.PlacementStatus;
import java.math.BigDecimal;

public record StudentResponse(
        Long id,
        String rollNumber,
        String fullName,
        String email,
        String department,
        BigDecimal cgpa,
        int activeBacklogs,
        PlacementStatus placementStatus,
        String companyPlaced,
        BigDecimal packageLpa) {
}
