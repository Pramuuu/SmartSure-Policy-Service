package com.smartSure.PolicyService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.ast.tree.expression.Every;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${notification.from-email}")
    private String fromEmail;

    // ── Policy Purchased ───────────────────────────────────

    // Sends a confirmation email to the customer after a policy is successfully purchased
    public void sendPolicyPurchasedEmail(
            String toEmail, String customerName, String policyNumber,
            String policyTypeName, BigDecimal coverageAmount,
            BigDecimal premiumAmount, String paymentFrequency,
            LocalDate startDate, LocalDate endDate) {

        String subject = "Policy Purchased — " + policyNumber;
        String body = """
                Dear %s,

                Your insurance policy has been successfully purchased.

                Policy Number : %s
                Policy Type   : %s
                Coverage      : ₹%s
                Premium       : ₹%s (%s)
                Start Date    : %s
                End Date      : %s

                Thank you for choosing SmartSure.
                """.formatted(customerName, policyNumber, policyTypeName,
                coverageAmount, premiumAmount, paymentFrequency, startDate, endDate);

        send(toEmail, subject, body);
    }

    // ── Premium Paid ───────────────────────────────────────

    // Sends a payment confirmation email to the customer after a premium installment is paid
    public void sendPremiumPaidEmail(
            String toEmail, String customerName, String policyNumber,
            BigDecimal amount, LocalDate paidDate,
            String paymentReference, String paymentMethod) {

        String subject = "Premium Payment Confirmed — " + policyNumber;
        String body = """
                Dear %s,

                Your premium payment has been recorded.

                Policy Number     : %s
                Amount Paid       : ₹%s
                Payment Date      : %s
                Payment Reference : %s
                Method            : %s

                Thank you for staying insured with SmartSure.
                """.formatted(customerName, policyNumber, amount,
                paidDate, paymentReference, paymentMethod);

        send(toEmail, subject, body);
    }

    // ── Policy Cancelled ───────────────────────────────────

    // Sends a cancellation notification email to the customer with the cancellation reason
    public void sendPolicyCancelledEmail(
            String toEmail, String customerName,
            String policyNumber, String cancellationReason) {

        String subject = "Policy Cancelled — " + policyNumber;
        String body = """
                Dear %s,

                Your policy %s has been cancelled.
                Reason: %s

                If this was a mistake, please contact our support team.

                SmartSure Support
                """.formatted(customerName, policyNumber,
                cancellationReason != null ? cancellationReason : "Not specified");

        send(toEmail, subject, body);
    }

    // ── Internal Send ──────────────────────────────────────

    // Core email sender — rethrows exceptions so RabbitMQ retry mechanism can count failures
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
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
            // Re-throw so RabbitMQ retry mechanism counts this as a failure
            throw ex;
        }
    }
    // Sends a reminder email to the customer when a premium installment is due soon
    public void sendPremiumDueReminderEmail(
            String toEmail, String customerName,
            String policyNumber, BigDecimal amount, LocalDate dueDate) {

        String subject = "Premium Due Reminder — " + policyNumber;
        String body = """
            Dear %s,

            This is a reminder that your premium payment is due soon.

            Policy Number : %s
            Amount Due    : ₹%s
            Due Date      : %s

            Please ensure timely payment to keep your policy active.

            SmartSure Team
            """.formatted(customerName, policyNumber, amount, dueDate);
        send(toEmail, subject, body);
    }

    // Sends a reminder email to the customer when their policy is about to expire
    public void sendPolicyExpiryReminderEmail(
            String toEmail, String customerName,
            String policyNumber, String policyTypeName, LocalDate endDate) {

        String subject = "Your Policy Expires Soon — " + policyNumber;
        String body = """
            Dear %s,

            Your %s policy %s is expiring on %s.

            Renew now to continue enjoying uninterrupted coverage.

            SmartSure Team
            """.formatted(customerName, policyTypeName, policyNumber, endDate);
        send(toEmail, subject, body);
    }
//```
//
//        ---
//
//        ## Final picture — zero direct `notificationService` calls anywhere in `PolicyService`
//
//    Every notification now flows through RabbitMQ:
//            ```
//    PolicyService  →  NotificationPublisher  →  RabbitMQ Queue
//                                                  ↓
//    NotificationConsumer
//                                                  ↓
//    NotificationService  →  Gmail SMTP
}














