package com.smartSure.PolicyService.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers common metric tags and enables @Timed on service methods.
 *
 * Every metric from this service will carry app=POLICYSERVICE
 * so Prometheus/Grafana can filter by service easily.
 */
@Configuration
public class ObservabilityConfig {

    // Adds app=POLICYSERVICE tag to every metric automatically
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags("app", "POLICYSERVICE");
    }

    // Enables @Timed annotation on any Spring bean method
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}