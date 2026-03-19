package com.smartSure.PolicyService.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Policy represents a purchased insurance policy by a customer.
 * Lifecycle: CREATED -> ACTIVE -> EXPIRED -> CANCELLED
 */
@Entity
@Table(name = "policies")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique policy number for reference
    @Column(nullable = false, unique = true)
    private String policyNumber;

    // Customer who owns this policy (ID from auth-service)
    @Column(nullable = false)
    private Long customerId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "policy_type_id", nullable = false)
    private PolicyType policyType;

    // Coverage details
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal coverageAmount;

    // Actual premium calculated for this customer
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal premiumAmount;

    // Payment frequency
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentFrequency paymentFrequency;

    // Policy dates
    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PolicyStatus status = PolicyStatus.CREATED;

    // Nominee / beneficiary information
    private String nomineeName;
    private String nomineeRelation;

    // Additional notes or remarks
    @Column(columnDefinition = "TEXT")
    private String remarks;

    // Cancellation reason if cancelled
    private String cancellationReason;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Premium> premiums;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * Policy Status Lifecycle:
     * Customer actions: CREATED -> ACTIVE
     * Admin actions:    ACTIVE -> CANCELLED
     * System actions:  ACTIVE -> EXPIRED (when endDate passes)
     */
    public enum PolicyStatus {
        CREATED,    // Policy purchased but not yet effective
        ACTIVE,     // Policy is active and coverage is in force
        EXPIRED,    // Policy term has ended
        CANCELLED   // Policy cancelled by customer or admin
    }

    public enum PaymentFrequency {
        MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL
    }
}






























