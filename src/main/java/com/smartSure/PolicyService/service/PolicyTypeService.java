package com.smartSure.PolicyService.service;

import com.smartSure.PolicyService.dto.policytype.PolicyTypeRequest;
import com.smartSure.PolicyService.dto.policytype.PolicyTypeResponse;
import com.smartSure.PolicyService.entity.PolicyType;

import java.util.List;

public interface PolicyTypeService {

    List<PolicyTypeResponse> getAllActivePolicyTypes();

    PolicyTypeResponse getPolicyTypeById(Long id);

    List<PolicyTypeResponse> getByCategory(PolicyType.InsuranceCategory category);

    List<PolicyTypeResponse> getAllPolicyTypes();

    PolicyTypeResponse createPolicyType(PolicyTypeRequest request);

    PolicyTypeResponse updatePolicyType(Long id, PolicyTypeRequest request);

    void deletePolicyType(Long id);
}