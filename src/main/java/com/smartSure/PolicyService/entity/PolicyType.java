package com.smartSure.PolicyService.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * PolicyType represents insurance product templates offered by the company.
 * Examples: Health Insurance, Auto Insurance, Home Insurance, Life Insurance
 */
@Entity
@Table(name = "policy_types")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;  // e.g. "Health Insurance", "Auto Insurance"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InsuranceCategory category;

    // Base premium amount per month
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basePremium;

    // Maximum coverage amount
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal maxCoverageAmount;

    // Deductible amount customer must pay before insurance kicks in
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal deductibleAmount;

    // Term in months (e.g. 12 for annual, 6 for semi-annual)
    @Column(nullable = false)
    private Integer termMonths;

    // Minimum and maximum age for eligibility
    private Integer minAge;
    private Integer maxAge;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PolicyTypeStatus status = PolicyTypeStatus.ACTIVE;

    // Premium rate factors stored as JSON-like text for flexibility
    @Column(columnDefinition = "TEXT")
    private String coverageDetails;

    @OneToMany(mappedBy = "policyType", fetch = FetchType.LAZY)
    private List<Policy> policies;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum InsuranceCategory {
        HEALTH, AUTO, HOME, LIFE, TRAVEL, BUSINESS
    }

    public enum PolicyTypeStatus {
        ACTIVE, INACTIVE, DISCONTINUED
    }
}





































