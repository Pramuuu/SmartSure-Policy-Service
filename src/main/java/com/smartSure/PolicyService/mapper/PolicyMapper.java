package com.smartSure.PolicyService.mapper;

import com.smartSure.PolicyService.dto.policy.PolicyPurchaseRequest;
import com.smartSure.PolicyService.dto.policy.PolicyResponse;
import com.smartSure.PolicyService.dto.premium.PremiumResponse;
import com.smartSure.PolicyService.entity.Policy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PremiumMapper.class, PolicyTypeMapper.class})
public interface PolicyMapper {

    @Mapping(target = "status", expression = "java(policy.getStatus().name())")
    @Mapping(target = "paymentFrequency", expression = "java(policy.getPaymentFrequency().name())")
    @Mapping(target = "policyType", source = "policy.policyType")
    @Mapping(target = "premiums", ignore = true)
    PolicyResponse toResponse(Policy policy);

    @Mapping(target = "status", expression = "java(policy.getStatus().name())")
    @Mapping(target = "paymentFrequency", expression = "java(policy.getPaymentFrequency().name())")
    @Mapping(target = "policyType", source = "policy.policyType")
    @Mapping(target = "premiums", source = "premiums")
    PolicyResponse toResponseWithPremiums(Policy policy, List<PremiumResponse> premiums);

    Policy toEntity(PolicyPurchaseRequest request);
}