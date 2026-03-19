//package com.smartSure.PolicyService.repository;
//
//
//import com.smartSure.PolicyService.entity.Premium;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//
//public interface PremiumRepository extends JpaRepository<Premium, Long> {
//
//    List<Premium> findByPolicyId(Long policyId);
//
//    List<Premium> findByPolicyIdAndStatus(Long policyId, Premium.PremiumStatus status);
//
//    List<Premium> findByStatus(Premium.PremiumStatus status);
//
//    // Overdue premiums
//    @Query("SELECT p FROM Premium p WHERE p.status = :status AND p.dueDate < :today")
//    List<Premium> findOverduePremiums(@Param("status") Premium.PremiumStatus status,
//                                      @Param("today") LocalDate today);
//
//    // Validate premium belongs to policy
//    Optional<Premium> findByIdAndPolicyId(Long premiumId, Long policyId);
//
//    // Total paid for a specific policy
//    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Premium p WHERE p.policy.id = :policyId AND p.status = :status")
//    BigDecimal totalPaidAmountByPolicy(@Param("policyId") Long policyId,
//                                       @Param("status") Premium.PremiumStatus status);
//
//    // Total collected across system
//    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Premium p WHERE p.status = :status")
//    BigDecimal totalPremiumCollected(@Param("status") Premium.PremiumStatus status);
//
//    long countByStatus(Premium.PremiumStatus status);
//}

package com.smartSure.PolicyService.repository;

import com.smartSure.PolicyService.entity.Premium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PremiumRepository extends JpaRepository<Premium, Long> {

    List<Premium> findByPolicyId(Long policyId);

    List<Premium> findByPolicyIdAndStatus(Long policyId, Premium.PremiumStatus status);

    List<Premium> findByStatus(Premium.PremiumStatus status);

    @Query("SELECT p FROM Premium p WHERE p.status = :status AND p.dueDate < :today")
    List<Premium> findOverduePremiums(@Param("status") Premium.PremiumStatus status,
                                      @Param("today") LocalDate today);

    Optional<Premium> findByIdAndPolicyId(Long premiumId, Long policyId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Premium p WHERE p.policy.id = :policyId AND p.status = :status")
    BigDecimal totalPaidAmountByPolicy(@Param("policyId") Long policyId,
                                       @Param("status") Premium.PremiumStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Premium p WHERE p.status = :status")
    BigDecimal totalPremiumCollected(@Param("status") Premium.PremiumStatus status);

    long countByStatus(Premium.PremiumStatus status);
}