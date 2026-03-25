package com.smartSure.PolicyService.service;

import com.smartSure.PolicyService.dto.calculation.PremiumCalculationRequest;
import com.smartSure.PolicyService.dto.calculation.PremiumCalculationResponse;
import com.smartSure.PolicyService.dto.event.PolicyCancelledEvent;
import com.smartSure.PolicyService.dto.event.PolicyExpiryReminderEvent;
import com.smartSure.PolicyService.dto.event.PolicyPurchasedEvent;
import com.smartSure.PolicyService.dto.event.PremiumDueReminderEvent;
import com.smartSure.PolicyService.dto.event.PremiumPaidEvent;
import com.smartSure.PolicyService.dto.policy.*;
import com.smartSure.PolicyService.dto.premium.PremiumPaymentRequest;
import com.smartSure.PolicyService.dto.premium.PremiumResponse;
import com.smartSure.PolicyService.entity.AuditLog;
import com.smartSure.PolicyService.entity.Policy;
import com.smartSure.PolicyService.entity.PolicyType;
import com.smartSure.PolicyService.entity.Premium;
import com.smartSure.PolicyService.exception.*;
import com.smartSure.PolicyService.mapper.PolicyMapper;
import com.smartSure.PolicyService.repository.AuditLogRepository;
import com.smartSure.PolicyService.repository.PolicyRepository;
import com.smartSure.PolicyService.repository.PolicyTypeRepository;
import com.smartSure.PolicyService.repository.PremiumRepository;
import com.smartSure.PolicyService.client.AuthServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository       policyRepository;
    private final PolicyTypeRepository   policyTypeRepository;
    private final PremiumRepository      premiumRepository;
    private final AuditLogRepository     auditLogRepository;
    private final PremiumCalculator      premiumCalculator;
    private final PolicyMapper           policyMapper;
    private final NotificationPublisher  notificationPublisher;
    private final AuthServiceClient      authServiceClient;

    // ═══════════════════════════════════════════════════════════
    // PURCHASE
    // ═══════════════════════════════════════════════════════════

    // Validates policy type, checks duplicates, calculates premium, saves policy, generates schedule, and fires purchase event
//    @CircuitBreaker(name = "policyTypeService", fallbackMethod = "purchaseFallback")
//    @RateLimiter(name = "policyPurchase", fallbackMethod = "purchaseRateLimitFallback")
    @Transactional
    public PolicyResponse purchasePolicy(Long customerId, PolicyPurchaseRequest request) {

        log.info("Purchase request — customer={}, policyTypeId={}", customerId, request.getPolicyTypeId());

        PolicyType type = policyTypeRepository.findById(request.getPolicyTypeId())
                .orElseThrow(() -> new PolicyTypeNotFoundException(request.getPolicyTypeId()));

        if (type.getStatus() != PolicyType.PolicyTypeStatus.ACTIVE) {
            throw new InactivePolicyTypeException(type.getName());
        }

        boolean alreadyExists = policyRepository.existsByCustomerIdAndPolicyType_IdAndStatusIn(
                customerId, type.getId(),
                List.of(Policy.PolicyStatus.CREATED, Policy.PolicyStatus.ACTIVE));
        if (alreadyExists) throw new DuplicatePolicyException();

        if (request.getCoverageAmount().compareTo(type.getMaxCoverageAmount()) > 0) {
            throw new CoverageExceedsLimitException(
                    request.getCoverageAmount(), type.getMaxCoverageAmount());
        }

        BigDecimal premiumAmount = premiumCalculator.calculatePremium(
                type, request.getCoverageAmount(),
                request.getPaymentFrequency(), request.getCustomerAge()
        ).getCalculatedPremium();

        Policy policy = policyMapper.toEntity(request);
        policy.setCustomerId(customerId);
        policy.setPolicyType(type);
        policy.setPremiumAmount(premiumAmount);
        policy.setPolicyNumber(generatePolicyNumber());
        policy.setEndDate(request.getStartDate().plusMonths(type.getTermMonths()));
        policy.setStatus(request.getStartDate().isAfter(LocalDate.now())
                ? Policy.PolicyStatus.CREATED : Policy.PolicyStatus.ACTIVE);

        Policy saved = policyRepository.save(policy);
        generatePremiumSchedule(saved, type.getTermMonths());
        saveAudit(saved.getId(), customerId, "CUSTOMER", "PURCHASED",
                null, saved.getStatus().name(), "New policy purchased");

        notificationPublisher.publishPolicyPurchased(
                PolicyPurchasedEvent.builder()
                        .policyId(saved.getId())
                        .policyNumber(saved.getPolicyNumber())
                        .customerId(customerId)
                        .customerEmail(getCustomerEmailSafely(customerId))
                        .customerName("Customer")
                        .policyTypeName(type.getName())
                        .coverageAmount(saved.getCoverageAmount())
                        .premiumAmount(saved.getPremiumAmount())
                        .paymentFrequency(saved.getPaymentFrequency().name())
                        .startDate(saved.getStartDate())
                        .endDate(saved.getEndDate())
                        .status(saved.getStatus().name())
                        .nomineeName(saved.getNomineeName())
                        .build());

        log.info("Policy created — policyId={}, policyNumber={}",
                saved.getId(), saved.getPolicyNumber());
        return buildDetailedResponse(saved);
    }

    // Circuit breaker fallback for purchasePolicy — throws ServiceUnavailableException
    public PolicyResponse purchaseFallback(
            Long customerId, PolicyPurchaseRequest request, Throwable t) {
        log.error("purchasePolicy CIRCUIT BREAKER fallback — customerId={}, reason={}",
                customerId, t.getMessage());
        throw new ServiceUnavailableException("Policy purchase service", t);
    }

    // Rate limiter fallback for purchasePolicy — thrown when too many requests are made
    public PolicyResponse purchaseRateLimitFallback(
            Long customerId, PolicyPurchaseRequest request, Throwable t) {
        log.warn("purchasePolicy RATE LIMIT fallback — customerId={}", customerId);
        throw new ServiceUnavailableException(
                "Too many purchase requests. Please wait a moment and try again.");
    }

    // ═══════════════════════════════════════════════════════════
    // GET — paginated
    // ═══════════════════════════════════════════════════════════

    // Returns a paginated list of all policies belonging to the given customer
    @Transactional(readOnly = true)
    public PolicyPageResponse getCustomerPolicies(Long customerId, Pageable pageable) {
        Page<Policy> page = policyRepository.findByCustomerId(customerId, pageable);
        return toPageResponse(page);
    }

    // Fetches a single policy by ID; enforces ownership check unless caller is admin
    @Transactional(readOnly = true)
    public PolicyResponse getPolicyById(Long policyId, Long userId, boolean isAdmin) {
        Policy policy = getPolicy(policyId);
        if (!isAdmin && !policy.getCustomerId().equals(userId)) {
            throw new UnauthorizedAccessException();
        }
        return buildDetailedResponse(policy);
    }

    // Returns all policies in the system with pagination — admin use only
    @Transactional(readOnly = true)
    public PolicyPageResponse getAllPolicies(Pageable pageable) {
        Page<Policy> page = policyRepository.findAll(pageable);
        return toPageResponse(page);
    }

    // ═══════════════════════════════════════════════════════════
    // CANCEL
    // ═══════════════════════════════════════════════════════════

    // Cancels the policy, waives all pending premiums, audits the change, and fires cancellation event
    @CircuitBreaker(name = "policyTypeService", fallbackMethod = "cancelFallback")
    @Transactional
    public PolicyResponse cancelPolicy(Long policyId, Long customerId, String reason) {

        Policy policy = getPolicy(policyId);

        if (!policy.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("You can only cancel your own policies");
        }
        if (policy.getStatus() == Policy.PolicyStatus.CANCELLED) {
            throw new IllegalStateException("Policy is already cancelled");
        }
        if (policy.getStatus() == Policy.PolicyStatus.EXPIRED) {
            throw new IllegalStateException("Expired policies cannot be cancelled");
        }

        String prevStatus = policy.getStatus().name();
        policy.setStatus(Policy.PolicyStatus.CANCELLED);
        policy.setCancellationReason(reason);

        premiumRepository.findByPolicyIdAndStatus(policyId, Premium.PremiumStatus.PENDING)
                .forEach(p -> p.setStatus(Premium.PremiumStatus.WAIVED));

        Policy saved = policyRepository.save(policy);
        saveAudit(policyId, customerId, "CUSTOMER", "CANCELLED",
                prevStatus, Policy.PolicyStatus.CANCELLED.name(), reason);

        notificationPublisher.publishPolicyCancelled(
                PolicyCancelledEvent.builder()
                        .policyId(saved.getId())
                        .policyNumber(saved.getPolicyNumber())
                        .customerId(customerId)
                        .customerEmail(getCustomerEmailSafely(customerId))
                        .customerName("Customer")
                        .cancellationReason(reason)
                        .build());

        return policyMapper.toResponse(saved);
    }

    // Circuit breaker fallback for cancelPolicy — throws ServiceUnavailableException
    public PolicyResponse cancelFallback(
            Long policyId, Long customerId, String reason, Throwable t) {
        log.error("cancelPolicy CIRCUIT BREAKER fallback — policyId={}, reason={}",
                policyId, t.getMessage());
        throw new ServiceUnavailableException("Policy cancellation service", t);
    }

    // ═══════════════════════════════════════════════════════════
    // RENEW
    // ═══════════════════════════════════════════════════════════

    // Expires the old policy, creates a renewed one with a fresh premium schedule, and notifies the customer
    @Transactional
    public PolicyResponse renewPolicy(Long customerId, PolicyRenewalRequest request) {

        Policy oldPolicy = getPolicy(request.getPolicyId());

        if (!oldPolicy.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("You can only renew your own policies");
        }

        oldPolicy.setStatus(Policy.PolicyStatus.EXPIRED);

        PolicyType type = oldPolicy.getPolicyType();

        BigDecimal coverage = request.getNewCoverageAmount() != null
                ? request.getNewCoverageAmount()
                : oldPolicy.getCoverageAmount();

        if (coverage.compareTo(type.getMaxCoverageAmount()) > 0) {
            throw new CoverageExceedsLimitException(coverage, type.getMaxCoverageAmount());
        }

        Policy.PaymentFrequency freq = request.getPaymentFrequency() != null
                ? request.getPaymentFrequency()
                : oldPolicy.getPaymentFrequency();

        BigDecimal premium = premiumCalculator
                .calculatePremium(type, coverage, freq, null)
                .getCalculatedPremium();

        Policy newPolicy = Policy.builder()
                .policyNumber(generatePolicyNumber())
                .customerId(customerId)
                .policyType(type)
                .coverageAmount(coverage)
                .premiumAmount(premium)
                .paymentFrequency(freq)
                .startDate(oldPolicy.getEndDate())
                .endDate(request.getNewEndDate())
                .status(Policy.PolicyStatus.ACTIVE)
                .build();

        Policy saved = policyRepository.save(newPolicy);
        generatePremiumSchedule(saved, type.getTermMonths());
        saveAudit(saved.getId(), customerId, "CUSTOMER", "RENEWED",
                null, Policy.PolicyStatus.ACTIVE.name(),
                "Renewed from policy " + oldPolicy.getPolicyNumber());

        // Renewal uses same event as purchase — customer gets a "policy renewed" email
        notificationPublisher.publishPolicyPurchased(
                PolicyPurchasedEvent.builder()
                        .policyId(saved.getId())
                        .policyNumber(saved.getPolicyNumber())
                        .customerId(customerId)
                        .customerEmail(getCustomerEmailSafely(customerId))
                        .customerName("Customer")
                        .policyTypeName(type.getName())
                        .coverageAmount(saved.getCoverageAmount())
                        .premiumAmount(saved.getPremiumAmount())
                        .paymentFrequency(saved.getPaymentFrequency().name())
                        .startDate(saved.getStartDate())
                        .endDate(saved.getEndDate())
                        .status(saved.getStatus().name())
                        .nomineeName(saved.getNomineeName())
                        .build());

        return buildDetailedResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    // PREMIUM PAYMENT
    // ═══════════════════════════════════════════════════════════

    // Marks a premium installment as PAID, auto-generates a reference if missing, and fires premium paid event
    @CircuitBreaker(name = "policyTypeService", fallbackMethod = "payPremiumFallback")
    @Transactional
    public PremiumResponse payPremium(Long customerId, PremiumPaymentRequest request) {

        Policy policy = getPolicy(request.getPolicyId());

        if (!policy.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("You can only pay premiums for your own policies");
        }

        Premium premium = premiumRepository
                .findByIdAndPolicyId(request.getPremiumId(), request.getPolicyId())
                .orElseThrow(() -> new PremiumNotFoundException(
                        request.getPremiumId(), request.getPolicyId()));

        if (premium.getStatus() == Premium.PremiumStatus.PAID) {
            throw new IllegalStateException("Premium is already paid");
        }
        if (premium.getStatus() == Premium.PremiumStatus.WAIVED) {
            throw new IllegalStateException("Waived premiums cannot be paid");
        }

        premium.setStatus(Premium.PremiumStatus.PAID);
        premium.setPaidDate(LocalDate.now());
        premium.setPaymentMethod(request.getPaymentMethod());
        premium.setPaymentReference(request.getPaymentReference() != null
                ? request.getPaymentReference()
                : "TXN-" + UUID.randomUUID().toString().substring(0, 8));

        Premium saved = premiumRepository.save(premium);
        saveAudit(policy.getId(), customerId, "CUSTOMER", "PREMIUM_PAID",
                Premium.PremiumStatus.PENDING.name(), Premium.PremiumStatus.PAID.name(),
                "Premium ID: " + premium.getId() + ", Ref: " + premium.getPaymentReference());

        notificationPublisher.publishPremiumPaid(
                PremiumPaidEvent.builder()
                        .premiumId(saved.getId())
                        .policyId(policy.getId())
                        .policyNumber(policy.getPolicyNumber())
                        .customerId(customerId)
                        .customerEmail(getCustomerEmailSafely(customerId))
                        .customerName("Customer")
                        .amount(saved.getAmount())
                        .paidDate(saved.getPaidDate())
                        .paymentMethod(saved.getPaymentMethod() != null
                                ? saved.getPaymentMethod().name() : null)
                        .paymentReference(saved.getPaymentReference())
                        .build());

        return mapPremium(saved);
    }

    // Circuit breaker fallback for payPremium — throws ServiceUnavailableException
    public PremiumResponse payPremiumFallback(
            Long customerId, PremiumPaymentRequest request, Throwable t) {
        log.error("payPremium CIRCUIT BREAKER fallback — customerId={}, reason={}",
                customerId, t.getMessage());
        throw new ServiceUnavailableException("Premium payment service", t);
    }

    // Returns all premium installments for the given policy
    @Transactional(readOnly = true)
    public List<PremiumResponse> getPremiumsByPolicy(Long policyId) {
        return premiumRepository.findByPolicyId(policyId)
                .stream()
                .map(this::mapPremium)
                .toList();
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN
    // ═══════════════════════════════════════════════════════════

    // Admin-only: force-updates a policy status and records an audit entry with role ADMIN
    @Transactional
    public PolicyResponse adminUpdatePolicyStatus(
            Long policyId, PolicyStatusUpdateRequest request) {

        Policy policy = getPolicy(policyId);
        String prevStatus = policy.getStatus().name();

        policy.setStatus(request.getStatus());
        if (request.getReason() != null) {
            policy.setCancellationReason(request.getReason());
        }

        Policy saved = policyRepository.save(policy);
        saveAudit(policyId, 0L, "ADMIN", "STATUS_CHANGED",
                prevStatus, request.getStatus().name(), request.getReason());

        return policyMapper.toResponse(saved);
    }

    // Returns system-wide counts by status, total premium collected, and total active coverage
    @Transactional(readOnly = true)
    public PolicySummaryResponse getPolicySummary() {
        return PolicySummaryResponse.builder()
                .totalPolicies(policyRepository.count())
                .activePolicies(policyRepository.countByStatus(Policy.PolicyStatus.ACTIVE))
                .expiredPolicies(policyRepository.countByStatus(Policy.PolicyStatus.EXPIRED))
                .cancelledPolicies(policyRepository.countByStatus(Policy.PolicyStatus.CANCELLED))
                .totalPremiumCollected(
                        premiumRepository.totalPremiumCollected(Premium.PremiumStatus.PAID))
                .totalCoverageProvided(policyRepository.sumActiveCoverages())
                .build();
    }

    // ═══════════════════════════════════════════════════════════
    // PREMIUM CALCULATION
    // ═══════════════════════════════════════════════════════════

    // Calculates a premium quote without persisting anything — used for pre-purchase estimates
    @Transactional(readOnly = true)
    public PremiumCalculationResponse calculatePremium(PremiumCalculationRequest request) {
        PolicyType type = policyTypeRepository.findById(request.getPolicyTypeId())
                .orElseThrow(() -> new PolicyTypeNotFoundException(request.getPolicyTypeId()));
        return premiumCalculator.calculatePremium(
                type, request.getCoverageAmount(),
                request.getPaymentFrequency(), request.getCustomerAge());
    }

    // ═══════════════════════════════════════════════════════════
    // SCHEDULERS
    // ═══════════════════════════════════════════════════════════

    // Runs daily at 01:00 — marks all active policies past their end date as EXPIRED
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void expirePolicies() {
        List<Policy> expired = policyRepository.findExpiredActivePolicies(
                Policy.PolicyStatus.ACTIVE, LocalDate.now());
        expired.forEach(p -> {
            p.setStatus(Policy.PolicyStatus.EXPIRED);
            saveAudit(p.getId(), 0L, "SYSTEM", "EXPIRED",
                    Policy.PolicyStatus.ACTIVE.name(),
                    Policy.PolicyStatus.EXPIRED.name(),
                    "Auto-expired by scheduler");
        });
        log.info("Expiry scheduler: {} policies expired", expired.size());
    }

    // Runs daily at 08:00 — marks all pending premiums past their due date as OVERDUE
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void markOverduePremiums() {
        List<Premium> overdue = premiumRepository.findOverduePremiums(
                Premium.PremiumStatus.PENDING, LocalDate.now());
        overdue.forEach(p -> p.setStatus(Premium.PremiumStatus.OVERDUE));
        log.info("Overdue scheduler: {} premiums marked overdue", overdue.size());
    }

    // Runs daily at 09:00 — publishes PREMIUM_DUE_REMINDER events for premiums due in 7 days
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void sendPremiumDueReminders() {
        LocalDate reminderDate = LocalDate.now().plusDays(7);
        premiumRepository.findByStatus(Premium.PremiumStatus.PENDING)
                .stream()
                .filter(p -> p.getDueDate().equals(reminderDate))
                .forEach(p -> {
                    String email = getCustomerEmailSafely(p.getPolicy().getCustomerId());
                    notificationPublisher.publishPremiumDueReminder(
                            PremiumDueReminderEvent.builder()
                                    .premiumId(p.getId())
                                    .policyId(p.getPolicy().getId())
                                    .policyNumber(p.getPolicy().getPolicyNumber())
                                    .customerId(p.getPolicy().getCustomerId())
                                    .customerEmail(email)
                                    .customerName("Customer")
                                    .amount(p.getAmount())
                                    .dueDate(p.getDueDate())
                                    .build());
                });
        log.info("Premium reminder scheduler fired for due date: {}", reminderDate);
    }

    // Runs daily at 09:05 — publishes POLICY_EXPIRY_REMINDER events for policies expiring in 30 days
    @Scheduled(cron = "0 5 9 * * *")
    @Transactional(readOnly = true)
    public void sendExpiryReminders() {
        LocalDate reminderDate = LocalDate.now().plusDays(30);
        policyRepository.findExpiringPolicies(
                        Policy.PolicyStatus.ACTIVE, reminderDate, reminderDate)
                .forEach(p -> {
                    String email = getCustomerEmailSafely(p.getCustomerId());
                    notificationPublisher.publishPolicyExpiryReminder(
                            PolicyExpiryReminderEvent.builder()
                                    .policyId(p.getId())
                                    .policyNumber(p.getPolicyNumber())
                                    .customerId(p.getCustomerId())
                                    .customerEmail(email)
                                    .customerName("Customer")
                                    .policyTypeName(p.getPolicyType().getName())
                                    .endDate(p.getEndDate())
                                    .build());
                });
        log.info("Expiry reminder scheduler fired for date: {}", reminderDate);
    }

    // ═══════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════

    // Fetches a policy by ID or throws PolicyNotFoundException
    private Policy getPolicy(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException(id));
    }

    // Builds a full PolicyResponse that includes the premium schedule
    private PolicyResponse buildDetailedResponse(Policy policy) {
        List<PremiumResponse> premiums = getPremiumsByPolicy(policy.getId());
        return policyMapper.toResponseWithPremiums(policy, premiums);
    }

    // Maps a Premium entity to a PremiumResponse DTO
    private PremiumResponse mapPremium(Premium premium) {
        return PremiumResponse.builder()
                .id(premium.getId())
                .amount(premium.getAmount())
                .dueDate(premium.getDueDate())
                .paidDate(premium.getPaidDate())
                .status(premium.getStatus().name())
                .paymentReference(premium.getPaymentReference())
                .paymentMethod(premium.getPaymentMethod() != null
                        ? premium.getPaymentMethod().name() : null)
                .build();
    }

    // Generates and saves all premium installments based on payment frequency and term length
    private void generatePremiumSchedule(Policy policy, int termMonths) {
        int interval = premiumCalculator.monthsBetweenInstallments(policy.getPaymentFrequency());
        int count    = premiumCalculator.installmentCount(termMonths, policy.getPaymentFrequency());
        LocalDate dueDate = policy.getStartDate();
        List<Premium> premiums = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            premiums.add(Premium.builder()
                    .policy(policy)
                    .amount(policy.getPremiumAmount())
                    .dueDate(dueDate)
                    .status(Premium.PremiumStatus.PENDING)
                    .build());
            dueDate = dueDate.plusMonths(interval);
        }
        premiumRepository.saveAll(premiums);
    }

    // Persists an audit log entry; swallows exceptions so audit failures never roll back the main transaction
    private void saveAudit(Long policyId, Long actorId, String actorRole,
                           String action, String fromStatus, String toStatus, String details) {
        try {
            auditLogRepository.save(AuditLog.builder()
                    .policyId(policyId)
                    .actorId(actorId)
                    .actorRole(actorRole)
                    .action(action)
                    .fromStatus(fromStatus)
                    .toStatus(toStatus)
                    .details(details)
                    .build());
        } catch (Exception ex) {
            log.error("Audit log save failed for policyId={}: {}", policyId, ex.getMessage());
        }
    }

    // Generates a unique policy number in the format POL-YYYYMMDD-XXXXX
    private String generatePolicyNumber() {
        return "POL-"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-"
                + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
    }

    // Converts a Page<Policy> into a PolicyPageResponse DTO
    private PolicyPageResponse toPageResponse(Page<Policy> page) {
        return PolicyPageResponse.builder()
                .content(page.getContent().stream().map(policyMapper::toResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    // Fetches customer email from Auth Service; returns null on failure so notifications never break the main flow
    private String getCustomerEmailSafely(Long customerId) {
        try {
            return authServiceClient.getCustomerEmail(customerId);
        } catch (Exception e) {
            log.warn("Could not fetch customer email for customerId={}: {}",
                    customerId, e.getMessage());
            return null;
        }
    }
}