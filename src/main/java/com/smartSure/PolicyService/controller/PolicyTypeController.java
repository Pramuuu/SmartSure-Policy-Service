package com.smartSure.PolicyService.controller;

import com.smartSure.PolicyService.dto.policytype.PolicyTypeRequest;
import com.smartSure.PolicyService.dto.policytype.PolicyTypeResponse;
import com.smartSure.PolicyService.entity.PolicyType;
import com.smartSure.PolicyService.service.PolicyTypeService;
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
@RequestMapping("/api/policy-types")
@RequiredArgsConstructor
@Tag(name = "Policy Types", description = "Insurance product catalog management")
public class PolicyTypeController {

    private final PolicyTypeService policyTypeService;

    @GetMapping
    @Operation(summary = "Get all active policy types (public)")
    public ResponseEntity<List<PolicyTypeResponse>> getActivePolicyTypes() {

        return ResponseEntity.ok(policyTypeService.getAllActivePolicyTypes());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get policy type by ID")
    public ResponseEntity<PolicyTypeResponse> getPolicyTypeById(@PathVariable Long id) {

        return ResponseEntity.ok(policyTypeService.getPolicyTypeById(id));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get policy types by insurance category")
    public ResponseEntity<List<PolicyTypeResponse>> getByCategory(
            @PathVariable PolicyType.InsuranceCategory category) {

        return ResponseEntity.ok(policyTypeService.getByCategory(category));
    }

    // ==================== ADMIN APIs ====================

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all policy types including inactive (Admin only)")
    public ResponseEntity<List<PolicyTypeResponse>> getAllPolicyTypes() {

        return ResponseEntity.ok(policyTypeService.getAllPolicyTypes());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new insurance product (Admin only)")
    public ResponseEntity<PolicyTypeResponse> createPolicyType(
            @Valid @RequestBody PolicyTypeRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(policyTypeService.createPolicyType(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update an insurance product (Admin only)")
    public ResponseEntity<PolicyTypeResponse> updatePolicyType(
            @PathVariable Long id,
            @Valid @RequestBody PolicyTypeRequest request) {

        return ResponseEntity.ok(
                policyTypeService.updatePolicyType(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Discontinue an insurance product (Admin only)")
    public ResponseEntity<String> deletePolicyType(@PathVariable Long id) {
        policyTypeService.deletePolicyType(id);

        return ResponseEntity.ok("Policy type discontinued successfully");
    }
}
