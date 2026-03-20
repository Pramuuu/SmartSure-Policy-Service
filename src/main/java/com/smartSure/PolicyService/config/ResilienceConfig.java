package com.smartSure.PolicyService.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();

        registry.getEventPublisher()
                .onEntryAdded(event -> {
                    var cb = event.getAddedEntry();

                    cb.getEventPublisher()
                            .onStateTransition(e ->
                                    log.warn("CircuitBreaker [{}] state changed: {} → {}",
                                            cb.getName(),
                                            e.getStateTransition().getFromState(),
                                            e.getStateTransition().getToState()))

                            .onCallNotPermitted(e ->
                                    log.warn("CircuitBreaker [{}] is OPEN — call rejected",
                                            cb.getName()))

                            .onError(e ->
                                    log.error("CircuitBreaker [{}] recorded failure: {}",
                                            cb.getName(), e.getThrowable().getMessage()))

                            .onSuccess(e ->
                                    log.debug("CircuitBreaker [{}] call succeeded in {}ms",
                                            cb.getName(), e.getElapsedDuration().toMillis()));
                });

        return registry;
    }

    @Bean
    public RetryRegistry retryRegistry() {

        RetryRegistry registry = RetryRegistry.ofDefaults();

        registry.getEventPublisher()
                .onEntryAdded(event -> {
                    var retry = event.getAddedEntry();

                    retry.getEventPublisher()
                            .onRetry(e ->
                                    log.warn("Retry [{}] attempt #{} after failure: {}",
                                            retry.getName(),
                                            e.getNumberOfRetryAttempts(),
                                            e.getLastThrowable().getMessage()))

                            .onError(e ->
                                    log.error("Retry [{}] exhausted all {} attempts",
                                            retry.getName(),
                                            e.getNumberOfRetryAttempts()))

                            .onSuccess(e ->
                                    log.info("Retry [{}] succeeded on attempt #{}",
                                            retry.getName(),
                                            e.getNumberOfRetryAttempts()));
                });

        return registry;
    }
}