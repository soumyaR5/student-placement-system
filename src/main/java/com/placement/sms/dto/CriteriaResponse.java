package com.placement.sms.dto;

import java.math.BigDecimal;

public record CriteriaResponse(
        Long id,
        String companyName,
        BigDecimal minCgpa,
        int maxActiveBacklogs) {
}
