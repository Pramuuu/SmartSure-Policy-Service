package com.smartSure.PolicyService.controller;

import com.smartSure.PolicyService.dto.calculation.PremiumCalculationRequest;
import com.smartSure.PolicyService.dto.calculation.PremiumCalculationResponse;
import com.smartSure.PolicyService.dto.policy.*;
import com.smartSure.PolicyService.dto.premium.PremiumPaymentRequest;
import com.smartSure.PolicyService.dto.premium.PremiumResponse;
import com.smartSure.PolicyService.service.PolicyService;
import com.smartSure.PolicyService.security.SecurityUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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
    public ResponseEntity<PolicyResponse> purchasePolicy(
            @Valid @RequestBody PolicyPurchaseRequest request) {

        Long customerId = SecurityUtils.getCurrentUserId();

        if (customerId == null) {
            throw new RuntimeException("Unauthorized: User not found in context");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(policyService.purchasePolicy(customerId, request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PolicyPageResponse> getMyPolicies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Long customerId = SecurityUtils.getCurrentUserId();

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return ResponseEntity.ok(
                policyService.getCustomerPolicies(customerId, PageRequest.of(page, size, sort))
        );
    }

    @GetMapping("/{policyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PolicyResponse> getPolicyById(@PathVariable Long policyId) {

        Long userId = SecurityUtils.getCurrentUserId();
        String role = SecurityUtils.getCurrentRole();

        boolean isAdmin = "ROLE_ADMIN".equals(role);

        return ResponseEntity.ok(
                policyService.getPolicyById(policyId, userId, isAdmin)
        );
    }

    @PutMapping("/{policyId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PolicyResponse> cancelPolicy(
            @PathVariable Long policyId,
            @RequestParam(required = false) String reason) {

        Long customerId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(
                policyService.cancelPolicy(policyId, customerId, reason)
        );
    }

    @PostMapping("/renew")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PolicyResponse> renewPolicy(
            @Valid @RequestBody PolicyRenewalRequest request) {

        Long customerId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(policyService.renewPolicy(customerId, request));
    }

    // ==================== PREMIUM PAYMENT ====================

    @PostMapping("/premiums/pay")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PremiumResponse> payPremium(
            @Valid @RequestBody PremiumPaymentRequest request) {

        Long customerId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(
                policyService.payPremium(customerId, request)
        );
    }

    @GetMapping("/{policyId}/premiums")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PremiumResponse>> getPremiums(
            @PathVariable Long policyId) {

        return ResponseEntity.ok(
                policyService.getPremiumsByPolicy(policyId)
        );
    }

    // ==================== PREMIUM CALCULATION ====================

    @PostMapping("/calculate-premium")
    public ResponseEntity<PremiumCalculationResponse> calculatePremium(
            @Valid @RequestBody PremiumCalculationRequest request) {

        return ResponseEntity.ok(
                policyService.calculatePremium(request)
        );
    }

    // ==================== ADMIN APIs ====================

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicyPageResponse> getAllPolicies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return ResponseEntity.ok(
                policyService.getAllPolicies(PageRequest.of(page, size, sort))
        );
    }

    @PutMapping("/admin/{policyId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicyResponse> adminUpdateStatus(
            @PathVariable Long policyId,
            @Valid @RequestBody PolicyStatusUpdateRequest request) {

        return ResponseEntity.ok(
                policyService.adminUpdatePolicyStatus(policyId, request)
        );
    }

    @GetMapping("/admin/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicySummaryResponse> getPolicySummary() {

        return ResponseEntity.ok(
                policyService.getPolicySummary()
        );
    }
}