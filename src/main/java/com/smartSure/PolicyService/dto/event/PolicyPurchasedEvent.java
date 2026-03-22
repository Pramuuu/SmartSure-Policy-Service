package com.smartSure.PolicyService.dto.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Message payload published to RabbitMQ when a policy is purchased.
 * Must be serializable to JSON — no JPA entities, only plain data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyPurchasedEvent {

    private Long       policyId;
    private String     policyNumber;
    private Long       customerId;
    private String     customerEmail;      // fetched from AuthService before publishing
    private String     customerName;
    private String     policyTypeName;
    private BigDecimal coverageAmount;
    private BigDecimal premiumAmount;
    private String     paymentFrequency;
    private LocalDate  startDate;
    private LocalDate  endDate;
    private String     status;
    private String     nomineeName;
    private LocalDateTime publishedAt;
}