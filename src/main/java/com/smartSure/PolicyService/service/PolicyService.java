package com.smartSure.PolicyService.service;

import com.smartSure.PolicyService.dto.calculation.PremiumCalculationRequest;
import com.smartSure.PolicyService.dto.calculation.PremiumCalculationResponse;
import com.smartSure.PolicyService.dto.policy.*;
import com.smartSure.PolicyService.dto.premium.PremiumPaymentRequest;
import com.smartSure.PolicyService.dto.premium.PremiumResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PolicyService {

    PolicyResponse purchasePolicy(Long customerId, PolicyPurchaseRequest request);

    PolicyPageResponse getCustomerPolicies(Long customerId, Pageable pageable);

    PolicyResponse getPolicyById(Long policyId, Long userId, boolean isAdmin);

    PolicyPageResponse getAllPolicies(Pageable pageable);

    PolicyResponse cancelPolicy(Long policyId, Long customerId, String reason);

    PolicyResponse renewPolicy(Long customerId, PolicyRenewalRequest request);

    PremiumResponse payPremium(Long customerId, PremiumPaymentRequest request);

    List<PremiumResponse> getPremiumsByPolicy(Long policyId);

    PolicyResponse adminUpdatePolicyStatus(Long policyId, PolicyStatusUpdateRequest request);

    PolicySummaryResponse getPolicySummary();

    PremiumCalculationResponse calculatePremium(PremiumCalculationRequest request);
}