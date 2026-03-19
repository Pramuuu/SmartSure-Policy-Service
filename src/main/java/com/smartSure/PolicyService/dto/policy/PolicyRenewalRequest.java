package com.smartSure.PolicyService.dto.policy;

import com.smartSure.PolicyService.entity.Policy;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyRenewalRequest {

    @NotNull(message = "Policy ID is required")
    private Long policyId;

    @NotNull(message = "New end date is required")
    private LocalDate newEndDate;

    private BigDecimal newCoverageAmount;
    private Policy.PaymentFrequency paymentFrequency;
}
