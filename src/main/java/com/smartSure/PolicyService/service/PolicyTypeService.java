package com.smartSure.PolicyService.service;


import com.smartSure.PolicyService.dto.policytype.PolicyTypeRequest;
import com.smartSure.PolicyService.dto.policytype.PolicyTypeResponse;
import com.smartSure.PolicyService.entity.PolicyType;
import com.smartSure.PolicyService.mapper.PolicyTypeMapper;
import com.smartSure.PolicyService.repository.PolicyTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyTypeService {

    private final PolicyTypeRepository policyTypeRepository;
    private final PolicyTypeMapper policyTypeMapper;

    // ==================== PUBLIC APIs ====================

    public List<PolicyTypeResponse> getAllActivePolicyTypes() {
        return policyTypeRepository
                .findByStatusOrderByCategory(PolicyType.PolicyTypeStatus.ACTIVE)
                .stream()
                .map(policyTypeMapper::toResponse)
                .toList();
    }

    public PolicyTypeResponse getPolicyTypeById(Long id) {
        PolicyType pt = getPolicyTypeEntity(id);
        return policyTypeMapper.toResponse(pt);
    }

    public List<PolicyTypeResponse> getByCategory(PolicyType.InsuranceCategory category) {
        return policyTypeRepository.findByCategory(category)
                .stream()
                .filter(pt -> pt.getStatus() == PolicyType.PolicyTypeStatus.ACTIVE)
                .map(policyTypeMapper::toResponse)
                .toList();
    }

    // ==================== ADMIN APIs ====================

    public List<PolicyTypeResponse> getAllPolicyTypes() {
        return policyTypeRepository.findAll()
                .stream()
                .map(policyTypeMapper::toResponse)
                .toList();
    }

    @Transactional
    public PolicyTypeResponse createPolicyType(PolicyTypeRequest request) {

        // Duplicate check
        if (policyTypeRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Policy type already exists with name: " + request.getName());
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

    @Transactional
    public PolicyTypeResponse updatePolicyType(Long id, PolicyTypeRequest request) {

        PolicyType pt = getPolicyTypeEntity(id);

        validateAgeRange(request.getMinAge(), request.getMaxAge());

        // Update fields
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

    @Transactional
    public void deletePolicyType(Long id) {
        PolicyType pt = getPolicyTypeEntity(id);

        // Soft delete
        pt.setStatus(PolicyType.PolicyTypeStatus.DISCONTINUED);

        policyTypeRepository.save(pt);
    }

    // ==================== PRIVATE HELPERS ====================

    private PolicyType getPolicyTypeEntity(Long id) {
        return policyTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Policy type not found with id: " + id));
    }

    private void validateAgeRange(Integer minAge, Integer maxAge) {
        if (minAge != null && maxAge != null && minAge > maxAge) {
            throw new IllegalArgumentException("Min age cannot be greater than max age");
        }
    }
}