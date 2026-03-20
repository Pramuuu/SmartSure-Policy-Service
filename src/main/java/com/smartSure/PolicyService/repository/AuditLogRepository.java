package com.smartSure.PolicyService.repository;

import com.smartSure.PolicyService.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByPolicyIdOrderByCreatedAtDesc(Long policyId);

    List<AuditLog> findByActorIdOrderByCreatedAtDesc(Long actorId);

    List<AuditLog> findByAction(String action);
}