package com.smartSure.PolicyService.repository;

import com.smartSure.PolicyService.entity.PolicyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PolicyTypeRepository extends JpaRepository<PolicyType, Long> {

    List<PolicyType> findByStatus(PolicyType.PolicyTypeStatus status);

    List<PolicyType> findByCategory(PolicyType.InsuranceCategory category);

    List<PolicyType> findByStatusOrderByCategory(PolicyType.PolicyTypeStatus status);

    Optional<PolicyType> findByName(String name);

    boolean existsByName(String name);

    long countByStatus(PolicyType.PolicyTypeStatus status);
}