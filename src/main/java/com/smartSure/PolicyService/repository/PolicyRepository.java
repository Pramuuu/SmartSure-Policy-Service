package com.smartSure.PolicyService.repository;

import com.smartSure.PolicyService.entity.Policy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    // ================= BASIC =================

    Optional<Policy> findByPolicyNumber(String policyNumber);

    List<Policy> findByCustomerId(Long customerId);

    Page<Policy> findByCustomerId(Long customerId, Pageable pageable);

    List<Policy> findByCustomerIdAndStatus(Long customerId, Policy.PolicyStatus status);

    List<Policy> findByStatus(Policy.PolicyStatus status);

    List<Policy> findByPolicyType_Id(Long policyTypeId);

    List<Policy> findByCustomerIdAndPolicyType_Id(Long customerId, Long policyTypeId);

    // ================= BUSINESS =================

    boolean existsByCustomerIdAndPolicyType_IdAndStatusIn(
            Long customerId,
            Long policyTypeId,
            List<Policy.PolicyStatus> statuses
    );

    // ================= EXPIRY / RENEWAL =================

    // Policies expiring soon (for reminders/notifications)
    @Query("SELECT p FROM Policy p WHERE p.status = :status AND p.endDate BETWEEN :startDate AND :endDate")
    List<Policy> findExpiringPolicies(
            @Param("status") Policy.PolicyStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Policies already expired but still ACTIVE (used by scheduler)
    @Query("SELECT p FROM Policy p WHERE p.status = :status AND p.endDate < :today")
    List<Policy> findExpiredActivePolicies(
            @Param("status") Policy.PolicyStatus status,
            @Param("today") LocalDate today
    );

    // ================= ANALYTICS =================

    long countByStatus(Policy.PolicyStatus status);

    long countByCustomerId(Long customerId);

    @Query("SELECT COALESCE(SUM(p.premiumAmount), 0) FROM Policy p WHERE p.status = 'ACTIVE'")
    BigDecimal sumActivePremiums();

    @Query("SELECT COALESCE(SUM(p.coverageAmount), 0) FROM Policy p WHERE p.status = 'ACTIVE'")
    BigDecimal sumActiveCoverages();
}