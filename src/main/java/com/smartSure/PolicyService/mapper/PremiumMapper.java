package com.smartSure.PolicyService.mapper;

import com.smartSure.PolicyService.dto.premium.PremiumResponse;
import com.smartSure.PolicyService.entity.Premium;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PremiumMapper {

    @Mapping(target = "status", expression = "java(premium.getStatus().name())")
    @Mapping(target = "paymentMethod", expression = "java(premium.getPaymentMethod() != null ? premium.getPaymentMethod().name() : null)")
    PremiumResponse toResponse(Premium premium);
}