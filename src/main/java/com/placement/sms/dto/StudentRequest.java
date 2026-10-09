package com.placement.sms.dto;

import com.placement.sms.entity.PlacementStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record StudentRequest(
        @NotBlank(message = "rollNumber is required")
        @Size(max = 30, message = "rollNumber must be at most 30 characters")
        String rollNumber,

        @NotBlank(message = "fullName is required")
        @Size(max = 100, message = "fullName must be at most 100 characters")
        String fullName,

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        @Size(max = 150, message = "email must be at most 150 characters")
        String email,

        @NotBlank(message = "department is required")
        @Size(max = 100, message = "department must be at most 100 characters")
        String department,

        @NotNull(message = "cgpa is required")
        @DecimalMin(value = "0.00", message = "cgpa must be at least 0.00")
        @DecimalMax(value = "10.00", message = "cgpa must be at most 10.00")
        @Digits(integer = 2, fraction = 2, message = "cgpa must have at most 2 decimal places")
        BigDecimal cgpa,

        @NotNull(message = "activeBacklogs is required")
        @Min(value = 0, message = "activeBacklogs must be at least 0")
        @Max(value = 50, message = "activeBacklogs must be at most 50")
        Integer activeBacklogs,

        PlacementStatus placementStatus,

        @Size(max = 100, message = "companyPlaced must be at most 100 characters")
        String companyPlaced,

        @DecimalMin(value = "0.00", message = "packageLpa must be at least 0.00")
        @Digits(integer = 6, fraction = 2, message = "packageLpa must have at most 2 decimal places")
        BigDecimal packageLpa) {
}
