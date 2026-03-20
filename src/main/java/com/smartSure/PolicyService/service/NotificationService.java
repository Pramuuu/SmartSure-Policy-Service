package com.smartSure.PolicyService.service;

import com.smartSure.PolicyService.entity.Policy;
import com.smartSure.PolicyService.entity.Premium;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Async email notifications for policy lifecycle events.
 *
 * NOTE: customerEmail is passed in from PolicyService which should
 * fetch it from AuthService via Feign. For now we accept it as a
 * parameter so the rest of the logic compiles independently.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${notification.from-email}")
    private String fromEmail;

    // ── Policy Purchase ────────────────────────────────────────
    @Async
    public void sendPolicyPurchasedEmail(String toEmail, Policy policy) {
        String subject = "Policy Purchased — " + policy.getPolicyNumber();
        String body = """
                Dear Customer,

                Your insurance policy has been successfully purchased.

                Policy Number : %s
                Policy Type   : %s
                Coverage      : ₹%s
                Premium       : ₹%s (%s)
                Start Date    : %s
                End Date      : %s
                Status        : %s

                Thank you for choosing SmartSure.
                """.formatted(
                policy.getPolicyNumber(),
                policy.getPolicyType().getName(),
                policy.getCoverageAmount(),
                policy.getPremiumAmount(),
                policy.getPaymentFrequency().name(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.getStatus().name()
        );
        send(toEmail, subject, body);
    }

    // ── Premium Payment Confirmed ──────────────────────────────
    @Async
    public void sendPremiumPaidEmail(String toEmail, Policy policy, Premium premium) {
        String subject = "Premium Payment Confirmed — " + policy.getPolicyNumber();
        String body = """
                Dear Customer,

                Your premium payment has been recorded.

                Policy Number     : %s
                Amount Paid       : ₹%s
                Payment Date      : %s
                Payment Reference : %s
                Method            : %s

                Thank you for staying insured with SmartSure.
                """.formatted(
                policy.getPolicyNumber(),
                premium.getAmount(),
                premium.getPaidDate(),
                premium.getPaymentReference(),
                premium.getPaymentMethod() != null ? premium.getPaymentMethod().name() : "N/A"
        );
        send(toEmail, subject, body);
    }

    // ── Policy Cancelled ───────────────────────────────────────
    @Async
    public void sendPolicyCancelledEmail(String toEmail, Policy policy) {
        String subject = "Policy Cancelled — " + policy.getPolicyNumber();
        String body = """
                Dear Customer,

                Your policy %s has been cancelled.
                Reason: %s

                If this was a mistake, please contact our support team.

                SmartSure Support
                """.formatted(
                policy.getPolicyNumber(),
                policy.getCancellationReason() != null ? policy.getCancellationReason() : "Not specified"
        );
        send(toEmail, subject, body);
    }

    // ── Premium Due Reminder ───────────────────────────────────
    @Async
    public void sendPremiumDueReminderEmail(String toEmail, Policy policy, Premium premium) {
        String subject = "Premium Due Reminder — " + policy.getPolicyNumber();
        String body = """
                Dear Customer,

                This is a reminder that your premium payment is due soon.

                Policy Number : %s
                Amount Due    : ₹%s
                Due Date      : %s

                Please ensure timely payment to keep your policy active.

                SmartSure Team
                """.formatted(
                policy.getPolicyNumber(),
                premium.getAmount(),
                premium.getDueDate()
        );
        send(toEmail, subject, body);
    }

    // ── Policy Expiry Reminder ─────────────────────────────────
    @Async
    public void sendPolicyExpiryReminderEmail(String toEmail, Policy policy) {
        String subject = "Your Policy Expires Soon — " + policy.getPolicyNumber();
        String body = """
                Dear Customer,

                Your policy %s is expiring on %s.

                Renew now to continue enjoying uninterrupted coverage.

                SmartSure Team
                """.formatted(
                policy.getPolicyNumber(),
                policy.getEndDate()
        );
        send(toEmail, subject, body);
    }

    // ── Internal Send ──────────────────────────────────────────
    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {} — subject: {}", to, subject);
        } catch (Exception ex) {
            // Never let email failure break the main transaction
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }
}