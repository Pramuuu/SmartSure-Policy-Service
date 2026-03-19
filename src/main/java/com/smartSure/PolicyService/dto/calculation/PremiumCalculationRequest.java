package com.smartSure.PolicyService.dto.calculation;

import com.smartSure.PolicyService.entity.Policy;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremiumCalculationRequest {

    @NotNull(message = "Policy type ID is required")
    private Long policyTypeId;

    @NotNull(message = "Coverage amount is required")
    private BigDecimal coverageAmount;

    @NotNull(message = "Payment frequency is required")
    private Policy.PaymentFrequency paymentFrequency;

    private Integer customerAge;
}
