package com.smartSure.PolicyService.client.fallback;

import com.smartSure.PolicyService.client.AuthServiceClient;
import com.smartSure.PolicyService.dto.client.CustomerProfileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback runs when AuthService is DOWN or circuit breaker is OPEN.
 *
 * Strategy:
 *  - Return a safe default profile so PolicyService operations
 *    (purchase, payment, cancel) still complete successfully.
 *  - Email notifications are silently skipped when email is unknown.
 *  - Never throw an exception from a fallback — that defeats the purpose.
 */
@Slf4j
@Component
public class AuthServiceFallback implements AuthServiceClient {

    @Override
    public CustomerProfileResponse getCustomerProfile(Long userId) {
        log.warn("AuthService FALLBACK triggered for getCustomerProfile — userId={}", userId);
        // Return a minimal safe profile — operations proceed without customer details
        return CustomerProfileResponse.builder()
                .id(userId)
                .name("Customer")
                .email(null)      // null email → NotificationService will skip sending
                .phone(null)
                .age(null)
                .build();
    }

    @Override
    public String getCustomerEmail(Long userId) {
        log.warn("AuthService FALLBACK triggered for getCustomerEmail — userId={}", userId);
        return null; // null → NotificationService checks and skips email gracefully
    }
}