package com.smartSure.PolicyService.client;

import com.smartSure.PolicyService.client.fallback.AuthServiceFallback;
import com.smartSure.PolicyService.dto.client.CustomerProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for AuthService.
 *
 * name        = Eureka service name (used when Eureka is enabled)
 * url         = direct URL (used when Eureka is disabled — for local testing)
 * fallback    = class that runs when circuit breaker is OPEN
 *
 * The @CircuitBreaker on the methods in PolicyService wraps calls
 * to this client. If AuthService is down, the fallback provides
 * safe default values so PolicyService can still function.
 */
@FeignClient(
        name = "authService",
        url = "${services.auth-service.url:http://localhost:8081}",
        fallback = AuthServiceFallback.class
)
public interface AuthServiceClient {

    @GetMapping("/user/internal/{userId}/profile")
    CustomerProfileResponse getCustomerProfile(@PathVariable("userId") Long userId);

    @GetMapping("/user/internal/{userId}/email")
    String getCustomerEmail(@PathVariable("userId") Long userId);
}