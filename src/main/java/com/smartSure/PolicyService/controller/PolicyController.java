package com.smartSure.PolicyService.controller;

import com.smartSure.PolicyService.dto.calculation.PremiumCalculationRequest;
import com.smartSure.PolicyService.dto.calculation.PremiumCalculationResponse;
import com.smartSure.PolicyService.dto.policy.*;
import com.smartSure.PolicyService.dto.premium.PremiumPaymentRequest;
import com.smartSure.PolicyService.dto.premium.PremiumResponse;
import com.smartSure.PolicyService.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Policies", description = "Policy purchase, management, and premium payment")
public class PolicyController {

    private final PolicyService policyService;

    // ==================== CUSTOMER APIs ====================

    @PostMapping("/purchase")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Purchase a new insurance policy")
    public ResponseEntity<PolicyResponse> purchasePolicy(
            @RequestHeader("X-User-Id") Long customerId,
            @Valid @RequestBody PolicyPurchaseRequest request) {



        return ResponseEntity.status(HttpStatus.CREATED)
                .body(policyService.purchasePolicy(customerId, request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get all my policies")
    public ResponseEntity<List<PolicyResponse>> getMyPolicies(
            @RequestHeader("X-User-Id") Long customerId) {



        return ResponseEntity.ok(policyService.getCustomerPolicies(customerId));
    }

    @GetMapping("/{policyId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get policy details by ID")
    public ResponseEntity<PolicyResponse> getPolicyById(
            @PathVariable Long policyId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {

        boolean isAdmin = "ADMIN".equals(role);

        return ResponseEntity.ok(
                policyService.getPolicyById(policyId, userId, isAdmin)
        );
    }

    @PutMapping("/{policyId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Cancel my policy")
    public ResponseEntity<PolicyResponse> cancelPolicy(
            @PathVariable Long policyId,
            @RequestHeader("X-User-Id") Long customerId,
            @RequestParam(required = false) String reason) {

        return ResponseEntity.ok(
                policyService.cancelPolicy(policyId, customerId, reason)
        );
    }

    @PostMapping("/renew")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Renew an existing policy")
    public ResponseEntity<PolicyResponse> renewPolicy(
            @RequestHeader("X-User-Id") Long customerId,
            @Valid @RequestBody PolicyRenewalRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(policyService.renewPolicy(customerId, request));
    }

    // ==================== PREMIUM PAYMENT ====================

    @PostMapping("/premiums/pay")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Pay a premium installment")
    public ResponseEntity<PremiumResponse> payPremium(
            @RequestHeader("X-User-Id") Long customerId,
            @Valid @RequestBody PremiumPaymentRequest request) {

        return ResponseEntity.ok(
                policyService.payPremium(customerId, request)
        );
    }

    @GetMapping("/{policyId}/premiums")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get premium schedule for a policy")
    public ResponseEntity<List<PremiumResponse>> getPremiums(
            @PathVariable Long policyId) {

        return ResponseEntity.ok(
                policyService.getPremiumsByPolicy(policyId)
        );
    }

    // ==================== PREMIUM CALCULATION ====================

    @PostMapping("/calculate-premium")
    @Operation(summary = "Calculate premium before purchase (no auth required for quote)")
    public ResponseEntity<PremiumCalculationResponse> calculatePremium(
            @Valid @RequestBody PremiumCalculationRequest request) {


        return ResponseEntity.ok(
                policyService.calculatePremium(request)
        );
    }

    // ==================== ADMIN APIs ====================

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all policies (Admin only)")
    public ResponseEntity<List<PolicyResponse>> getAllPolicies() {

        return ResponseEntity.ok(policyService.getAllPolicies());
    }

    @PutMapping("/admin/{policyId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update policy status (Admin only)")
    public ResponseEntity<PolicyResponse> adminUpdateStatus(
            @PathVariable Long policyId,
            @Valid @RequestBody PolicyStatusUpdateRequest request) {


        return ResponseEntity.ok(
                policyService.adminUpdatePolicyStatus(policyId, request)
        );
    }

    @GetMapping("/admin/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get policy statistics summary (Admin only)")
    public ResponseEntity<PolicySummaryResponse> getPolicySummary() {

        return ResponseEntity.ok(
                policyService.getPolicySummary()
        );
    }
}
