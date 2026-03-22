package com.smartSure.PolicyService.dto.event;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyExpiryReminderEvent {
    private Long          policyId;
    private String        policyNumber;
    private Long          customerId;
    private String        customerEmail;
    private String        customerName;
    private String        policyTypeName;
    private LocalDate     endDate;
    private LocalDateTime publishedAt;
}