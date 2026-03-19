package com.smartSure.PolicyService.dto.premium;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremiumResponse {

    private Long id;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private String status;
    private String paymentReference;
    private String paymentMethod;
}