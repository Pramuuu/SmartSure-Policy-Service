package com.smartSure.PolicyService.dto.policy;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicySummaryResponse {

    private Long totalPolicies;
    private Long activePolicies;
    private Long expiredPolicies;
    private Long cancelledPolicies;
    private BigDecimal totalPremiumCollected;
    private BigDecimal totalCoverageProvided;
}