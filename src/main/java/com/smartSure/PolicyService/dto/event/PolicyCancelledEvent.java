package com.smartSure.PolicyService.dto.event;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyCancelledEvent {

    private Long      policyId;
    private String    policyNumber;
    private Long      customerId;
    private String    customerEmail;
    private String    customerName;
    private String    cancellationReason;
    private LocalDateTime publishedAt;
}