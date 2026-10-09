package com.placement.sms.dto;

import java.util.List;

public record EligibilityResponse(
        Long studentId,
        String studentName,
        String rollNumber,
        Long criteriaId,
        String companyName,
        EligibilityStatus status,
        boolean eligible,
        List<String> reasons) {
}
