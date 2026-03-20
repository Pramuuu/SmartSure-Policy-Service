package com.smartSure.PolicyService.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Immutable audit trail of every status change on a policy.
 * Never update or delete rows — append only.
 */
@Entity
@Table(name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_policy", columnList = "policyId"),
                @Index(name = "idx_audit_actor",  columnList = "actorId")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which policy was changed
    @Column(nullable = false)
    private Long policyId;

    // Who made the change (customerId or adminId from auth-service)
    @Column(nullable = false)
    private Long actorId;

    @Column(nullable = false)
    private String actorRole; // CUSTOMER or ADMIN

    @Column(nullable = false)
    private String action; // PURCHASED, CANCELLED, RENEWED, STATUS_CHANGED, PREMIUM_PAID

    private String fromStatus;
    private String toStatus;

    @Column(columnDefinition = "TEXT")
    private String details; // optional JSON or free text

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
