package com.smartSure.PolicyService.service;

import com.smartSure.PolicyService.config.RabbitMQConfig;
import com.smartSure.PolicyService.dto.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Publishes notification events to RabbitMQ.
 *
 * PolicyService calls these methods after every business operation.
 * Publishing to RabbitMQ takes <1ms — it never blocks the HTTP response.
 * The actual email sending happens in NotificationConsumer (separate thread).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    // Publishes a POLICY_PURCHASED event to RabbitMQ after a policy is bought or renewed
    public void publishPolicyPurchased(PolicyPurchasedEvent event) {
        event.setPublishedAt(LocalDateTime.now());
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.KEY_POLICY_PURCHASED,
                    event
            );
            log.info("Published POLICY_PURCHASED event — policyId={}, customerId={}",
                    event.getPolicyId(), event.getCustomerId());
        } catch (Exception e) {
            // Log but never throw — RabbitMQ being down must not fail a purchase
            log.error("Failed to publish POLICY_PURCHASED event — policyId={}: {}",
                    event.getPolicyId(), e.getMessage());
        }
    }

    // Publishes a PREMIUM_PAID event to RabbitMQ after a premium installment is paid
    public void publishPremiumPaid(PremiumPaidEvent event) {
        event.setPublishedAt(LocalDateTime.now());
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.KEY_PREMIUM_PAID,
                    event
            );
            log.info("Published PREMIUM_PAID event — premiumId={}, policyId={}",
                    event.getPremiumId(), event.getPolicyId());
        } catch (Exception e) {
            log.error("Failed to publish PREMIUM_PAID event — premiumId={}: {}",
                    event.getPremiumId(), e.getMessage());
        }
    }

    // Publishes a POLICY_CANCELLED event to RabbitMQ after a policy is cancelled
    public void publishPolicyCancelled(PolicyCancelledEvent event) {
        event.setPublishedAt(LocalDateTime.now());
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.KEY_POLICY_CANCELLED,
                    event
            );
            log.info("Published POLICY_CANCELLED event — policyId={}, customerId={}",
                    event.getPolicyId(), event.getCustomerId());
        } catch (Exception e) {
            log.error("Failed to publish POLICY_CANCELLED event — policyId={}: {}",
                    event.getPolicyId(), e.getMessage());
        }
    }

    // Publishes a PREMIUM_DUE_REMINDER event to RabbitMQ for premiums due in 7 days
    public void publishPremiumDueReminder(PremiumDueReminderEvent event) {
        event.setPublishedAt(LocalDateTime.now());
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.KEY_PREMIUM_DUE_REMINDER,
                    event);
            log.info("Published PREMIUM_DUE_REMINDER event — premiumId={}, customerId={}",
                    event.getPremiumId(), event.getCustomerId());
        } catch (Exception e) {
            log.error("Failed to publish PREMIUM_DUE_REMINDER — premiumId={}: {}",
                    event.getPremiumId(), e.getMessage());
        }
    }

    // Publishes a POLICY_EXPIRY_REMINDER event to RabbitMQ for policies expiring in 30 days
    public void publishPolicyExpiryReminder(PolicyExpiryReminderEvent event) {
        event.setPublishedAt(LocalDateTime.now());
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.KEY_POLICY_EXPIRY_REMINDER,
                    event);
            log.info("Published POLICY_EXPIRY_REMINDER event — policyId={}, customerId={}",
                    event.getPolicyId(), event.getCustomerId());
        } catch (Exception e) {
            log.error("Failed to publish POLICY_EXPIRY_REMINDER — policyId={}: {}",
                    event.getPolicyId(), e.getMessage());
        }
    }
}
