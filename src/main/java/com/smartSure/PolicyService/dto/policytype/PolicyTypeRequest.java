package com.smartSure.PolicyService.dto.policytype;


import com.smartSure.PolicyService.entity.PolicyType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyTypeRequest {

    @NotBlank(message = "Policy type name is required")
    private String name;

    private String description;

    @NotNull(message = "Category is required")
    private PolicyType.InsuranceCategory category;

    @NotNull(message = "Base premium is required")
    @DecimalMin(value = "0.01", message = "Base premium must be positive")
    private BigDecimal basePremium;

    @NotNull(message = "Max coverage amount is required")
    @DecimalMin(value = "1000.00", message = "Max coverage must be at least 1000")
    private BigDecimal maxCoverageAmount;

    @NotNull(message = "Deductible amount is required")
    private BigDecimal deductibleAmount;

    @NotNull(message = "Term months is required")
    @Min(value = 1, message = "Term must be at least 1 month")
    private Integer termMonths;

    private Integer minAge;
    private Integer maxAge;
    private String coverageDetails;
}