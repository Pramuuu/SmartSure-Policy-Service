package com.smartSure.PolicyService.dto.policy;

import com.smartSure.PolicyService.entity.Policy;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyPurchaseRequest {

    @NotNull(message = "Policy type ID is required")
    private Long policyTypeId;

    @NotNull(message = "Coverage amount is required")
    @DecimalMin(value = "1000.00", message = "Coverage must be at least 1000")
    private BigDecimal coverageAmount;

    @NotNull(message = "Payment frequency is required")
    private Policy.PaymentFrequency paymentFrequency;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or future")
    private LocalDate startDate;

    private String nomineeName;
    private String nomineeRelation;
    private Integer customerAge;
}
