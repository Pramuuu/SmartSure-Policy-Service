package com.smartSure.PolicyService.service.impl;

import com.smartSure.PolicyService.dto.policytype.PolicyTypeRequest;
import com.smartSure.PolicyService.dto.policytype.PolicyTypeResponse;
import com.smartSure.PolicyService.entity.PolicyType;
import com.smartSure.PolicyService.exception.PolicyTypeNotFoundException;
import com.smartSure.PolicyService.exception.ServiceUnavailableException;
import com.smartSure.PolicyService.mapper.PolicyTypeMapper;
import com.smartSure.PolicyService.repository.PolicyTypeRepository;
import com.smartSure.PolicyService.service.PolicyTypeService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyTypeServiceImpl implements PolicyTypeService {

    private final PolicyTypeRepository policyTypeRepository;
    private final PolicyTypeMapper     policyTypeMapper;

    // ── Public ────────────────────────────────────────────────

    @Override
    @Cacheable("policyTypes")
    @CircuitBreaker(name = "policyTypeService", fallbackMethod = "getAllActiveFallback")
    public List<PolicyTypeResponse> getAllActivePolicyTypes() {
        return policyTypeRepository
                .findByStatusOrderByCategory(PolicyType.PolicyTypeStatus.ACTIVE)
                .stream()
                .map(policyTypeMapper::toResponse)
                .toList();
    }

    // Circuit breaker fallback — returns empty list so the API stays up when DB is unreachable
    public List<PolicyTypeResponse> getAllActiveFallback(Throwable t) {
        log.error("getAllActivePolicyTypes CIRCUIT BREAKER fallback — reason={}", t.getMessage());
        return List.of();
    }

    @Override
    @Cacheable(value = "policyById", key = "#id")
    @CircuitBreaker(name = "policyTypeService", fallbackMethod = "getPolicyTypeFallback")
    public PolicyTypeResponse getPolicyTypeById(Long id) {
        return policyTypeMapper.toResponse(getPolicyTypeEntity(id));
    }

    // Circuit breaker fallback — throws ServiceUnavailableException since we can't proceed without product details
    public PolicyTypeResponse getPolicyTypeFallback(Long id, Throwable t) {
        log.error("getPolicyTypeById CIRCUIT BREAKER fallback — id={}, reason={}", id, t.getMessage());
        throw new ServiceUnavailableException("Policy type lookup service", t);
    }

    @Override
    public List<PolicyTypeResponse> getByCategory(PolicyType.InsuranceCategory category) {
        return policyTypeRepository.findByCategory(category)
                .stream()
                .filter(pt -> pt.getStatus() == PolicyType.PolicyTypeStatus.ACTIVE)
                .map(policyTypeMapper::toResponse)
                .toList();
    }

    // ── Admin ─────────────────────────────────────────────────

    @Override
    public List<PolicyTypeResponse> getAllPolicyTypes() {
        return policyTypeRepository.findAll()
                .stream()
                .map(policyTypeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = {"policyTypes", "policyById"}, allEntries = true)
    public PolicyTypeResponse createPolicyType(PolicyTypeRequest request) {
        if (policyTypeRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Policy type already exists: " + request.getName());
        }
        validateAgeRange(request.getMinAge(), request.getMaxAge());

        PolicyType pt = PolicyType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .basePremium(request.getBasePremium())
                .maxCoverageAmount(request.getMaxCoverageAmount())
                .deductibleAmount(request.getDeductibleAmount())
                .termMonths(request.getTermMonths())
                .minAge(request.getMinAge())
                .maxAge(request.getMaxAge())
                .coverageDetails(request.getCoverageDetails())
                .status(PolicyType.PolicyTypeStatus.ACTIVE)
                .build();

        return policyTypeMapper.toResponse(policyTypeRepository.save(pt));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"policyTypes", "policyById"}, allEntries = true)
    public PolicyTypeResponse updatePolicyType(Long id, PolicyTypeRequest request) {
        PolicyType pt = getPolicyTypeEntity(id);
        validateAgeRange(request.getMinAge(), request.getMaxAge());

        pt.setName(request.getName());
        pt.setDescription(request.getDescription());
        pt.setCategory(request.getCategory());
        pt.setBasePremium(request.getBasePremium());
        pt.setMaxCoverageAmount(request.getMaxCoverageAmount());
        pt.setDeductibleAmount(request.getDeductibleAmount());
        pt.setTermMonths(request.getTermMonths());
        pt.setMinAge(request.getMinAge());
        pt.setMaxAge(request.getMaxAge());
        pt.setCoverageDetails(request.getCoverageDetails());

        return policyTypeMapper.toResponse(policyTypeRepository.save(pt));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"policyTypes", "policyById"}, allEntries = true)
    public void deletePolicyType(Long id) {
        PolicyType pt = getPolicyTypeEntity(id);
        pt.setStatus(PolicyType.PolicyTypeStatus.DISCONTINUED);
        policyTypeRepository.save(pt);
    }

    // ── Helpers ───────────────────────────────────────────────

    private PolicyType getPolicyTypeEntity(Long id) {
        return policyTypeRepository.findById(id)
                .orElseThrow(() -> new PolicyTypeNotFoundException(id));
    }

    private void validateAgeRange(Integer minAge, Integer maxAge) {
        if (minAge != null && maxAge != null && minAge > maxAge) {
            throw new IllegalArgumentException("Min age cannot be greater than max age");
        }
    }
}