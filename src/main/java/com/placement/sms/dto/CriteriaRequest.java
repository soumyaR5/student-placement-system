package com.placement.sms.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CriteriaRequest(
        @NotBlank(message = "companyName is required")
        @Size(max = 100, message = "companyName must be at most 100 characters")
        String companyName,

        @NotNull(message = "minCgpa is required")
        @DecimalMin(value = "0.00", message = "minCgpa must be at least 0.00")
        @DecimalMax(value = "10.00", message = "minCgpa must be at most 10.00")
        @Digits(integer = 2, fraction = 2, message = "minCgpa must have at most 2 decimal places")
        BigDecimal minCgpa,

        @NotNull(message = "maxActiveBacklogs is required")
        @Min(value = 0, message = "maxActiveBacklogs must be at least 0")
        @Max(value = 50, message = "maxActiveBacklogs must be at most 50")
        Integer maxActiveBacklogs) {
}
