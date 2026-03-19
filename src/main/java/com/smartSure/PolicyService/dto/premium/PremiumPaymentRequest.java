package com.smartSure.PolicyService.dto.premium;

import com.smartSure.PolicyService.entity.Premium;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremiumPaymentRequest {

    @NotNull(message = "Policy ID is required")
    private Long policyId;

    @NotNull(message = "Premium ID is required")
    private Long premiumId;

    @NotNull(message = "Payment method is required")
    private Premium.PaymentMethod paymentMethod;

    private String paymentReference;
}
