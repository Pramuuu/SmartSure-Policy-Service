package com.smartSure.PolicyService.dto.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremiumDueReminderEvent {
    private Long          premiumId;
    private Long          policyId;
    private String        policyNumber;
    private Long          customerId;
    private String        customerEmail;
    private String        customerName;
    private BigDecimal    amount;
    private LocalDate     dueDate;
    private LocalDateTime publishedAt;
}