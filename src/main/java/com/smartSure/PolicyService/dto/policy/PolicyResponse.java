package com.smartSure.PolicyService.dto.policy;

import com.smartSure.PolicyService.dto.policytype.PolicyTypeResponse;
import com.smartSure.PolicyService.dto.premium.PremiumResponse;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyResponse {

    private Long id;
    private String policyNumber;
    private Long customerId;
    private PolicyTypeResponse policyType;
    private BigDecimal coverageAmount;
    private BigDecimal premiumAmount;
    private String paymentFrequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String nomineeName;
    private String nomineeRelation;
    private String remarks;
    private String cancellationReason;
    private String createdAt;
    private List<PremiumResponse> premiums;
}
