package com.smartSure.PolicyService.dto.calculation;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremiumCalculationResponse {

    private BigDecimal basePremium;
    private BigDecimal calculatedPremium;
    private BigDecimal annualPremium;
    private String paymentFrequency;
    private String breakdown;
}