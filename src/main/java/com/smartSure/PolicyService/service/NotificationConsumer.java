package com.smartSure.PolicyService.service;

import com.smartSure.PolicyService.config.RabbitMQConfig;
import com.smartSure.PolicyService.dto.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Consumes notification events from RabbitMQ and sends emails.
 *
 * Each @RabbitListener runs in its own thread pool — completely
 * separate from the HTTP request threads. If email sending fails,
 * Spring Retry (configured in application.properties) retries
 * up to 3 times with exponential backoff. After 3 failures the
 * message goes to the Dead Letter Queue (DLQ) for manual review.
 *
 * This class is the ONLY place that calls NotificationService now.
 * PolicyService only calls NotificationPublisher.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    // Consumes POLICY_PURCHASED event and sends a purchase confirmation email to the customer
    @RabbitListener(queues = RabbitMQConfig.QUEUE_POLICY_PURCHASED)
    public void handlePolicyPurchased(PolicyPurchasedEvent event) {
        log.info("Consuming POLICY_PURCHASED event — policyId={}, email={}",
                event.getPolicyId(), event.getCustomerEmail());
        try {
            Thread.sleep(10000); //(10 sec delay)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (event.getCustomerEmail() == null || event.getCustomerEmail().isBlank()) {
            log.warn("Skipping email — no customer email in event for policyId={}", event.getPolicyId());
            return; // ack the message — no point retrying without an email address
        }

        notificationService.sendPolicyPurchasedEmail(
                event.getCustomerEmail(),
                event.getCustomerName(),
                event.getPolicyNumber(),
                event.getPolicyTypeName(),
                event.getCoverageAmount(),
                event.getPremiumAmount(),
                event.getPaymentFrequency(),
                event.getStartDate(),
                event.getEndDate()
        );
    }

    // Consumes PREMIUM_PAID event and sends a payment confirmation email to the customer
    @RabbitListener(queues = RabbitMQConfig.QUEUE_PREMIUM_PAID)
    public void handlePremiumPaid(PremiumPaidEvent event) {
        log.info("Consuming PREMIUM_PAID event — premiumId={}, policyId={}",
                event.getPremiumId(), event.getPolicyId());

        if (event.getCustomerEmail() == null || event.getCustomerEmail().isBlank()) {
            log.warn("Skipping email — no customer email for premiumId={}", event.getPremiumId());
            return;
        }

        notificationService.sendPremiumPaidEmail(
                event.getCustomerEmail(),
                event.getCustomerName(),
                event.getPolicyNumber(),
                event.getAmount(),
                event.getPaidDate(),
                event.getPaymentReference(),
                event.getPaymentMethod()
        );
    }

    // Consumes POLICY_CANCELLED event and sends a cancellation notification email to the customer
    @RabbitListener(queues = RabbitMQConfig.QUEUE_POLICY_CANCELLED)
    public void handlePolicyCancelled(PolicyCancelledEvent event) {
        log.info("Consuming POLICY_CANCELLED event — policyId={}", event.getPolicyId());

        if (event.getCustomerEmail() == null || event.getCustomerEmail().isBlank()) {
            log.warn("Skipping email — no customer email for policyId={}", event.getPolicyId());
            return;
        }

        notificationService.sendPolicyCancelledEmail(
                event.getCustomerEmail(),
                event.getCustomerName(),
                event.getPolicyNumber(),
                event.getCancellationReason()
        );
    }

    // Consumes PREMIUM_DUE_REMINDER event and sends a due date reminder email to the customer
    @RabbitListener(queues = RabbitMQConfig.QUEUE_PREMIUM_DUE_REMINDER)
    public void handlePremiumDueReminder(PremiumDueReminderEvent event) {
        log.info("Consuming PREMIUM_DUE_REMINDER event — premiumId={}", event.getPremiumId());
        if (event.getCustomerEmail() == null || event.getCustomerEmail().isBlank()) {
            log.warn("Skipping email — no customer email for premiumId={}", event.getPremiumId());
            return;
        }
        notificationService.sendPremiumDueReminderEmail(
                event.getCustomerEmail(),
                event.getCustomerName(),
                event.getPolicyNumber(),
                event.getAmount(),
                event.getDueDate());
    }

    // Consumes POLICY_EXPIRY_REMINDER event and sends a policy expiry reminder email to the customer
    @RabbitListener(queues = RabbitMQConfig.QUEUE_POLICY_EXPIRY_REMINDER)
    public void handlePolicyExpiryReminder(PolicyExpiryReminderEvent event) {
        log.info("Consuming POLICY_EXPIRY_REMINDER event — policyId={}", event.getPolicyId());
        if (event.getCustomerEmail() == null || event.getCustomerEmail().isBlank()) {
            log.warn("Skipping email — no customer email for policyId={}", event.getPolicyId());
            return;
        }
        notificationService.sendPolicyExpiryReminderEmail(
                event.getCustomerEmail(),
                event.getCustomerName(),
                event.getPolicyNumber(),
                event.getPolicyTypeName(),
                event.getEndDate());
    }
}