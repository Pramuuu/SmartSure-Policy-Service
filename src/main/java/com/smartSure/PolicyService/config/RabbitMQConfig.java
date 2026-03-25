package com.smartSure.PolicyService.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology for PolicyService notifications.
 *
 * Exchange: smartsure.notifications (Topic Exchange)
 *
 * Queues and routing keys:
 *   policy.purchased  → notification.policy.purchased
 *   premium.paid      → notification.premium.paid
 *   policy.cancelled  → notification.policy.cancelled
 *
 * Dead Letter Queue:
 *   If a message fails all 3 retry attempts, it goes to
 *   smartsure.notifications.dlq so nothing is ever lost.
 */
@Configuration
public class RabbitMQConfig {

    // ── Exchange names ─────────────────────────────────────
    public static final String EXCHANGE = "smartsure.notifications";
    public static final String DLQ_EXCHANGE = "smartsure.notifications.dlx";

    // ── Queue names ────────────────────────────────────────
    public static final String QUEUE_POLICY_PURCHASED        = "notification.policy.purchased";
    public static final String QUEUE_PREMIUM_PAID            = "notification.premium.paid";
    public static final String QUEUE_POLICY_CANCELLED        = "notification.policy.cancelled";
    public static final String QUEUE_PREMIUM_DUE_REMINDER    = "notification.premium.due.reminder";
    public static final String QUEUE_POLICY_EXPIRY_REMINDER  = "notification.policy.expiry.reminder";
    public static final String DLQ                           = "smartsure.notifications.dlq";

    // ── Routing keys ───────────────────────────────────────
    public static final String KEY_POLICY_PURCHASED       = "policy.purchased";
    public static final String KEY_PREMIUM_PAID           = "premium.paid";
    public static final String KEY_POLICY_CANCELLED       = "policy.cancelled";
    public static final String KEY_PREMIUM_DUE_REMINDER   = "premium.due.reminder";
    public static final String KEY_POLICY_EXPIRY_REMINDER = "policy.expiry.reminder";

    // ── Exchange ───────────────────────────────────────────

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLQ_EXCHANGE, true, false);
    }

    // ── Dead Letter Queue (catches all failed messages) ────

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("#");  // # matches everything
    }

    // ── Policy Purchased Queue ─────────────────────────────

    @Bean
    public Queue policyPurchasedQueue() {
        return QueueBuilder
                .durable(QUEUE_POLICY_PURCHASED)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.policy.purchased")
                .build();
    }

    @Bean
    public Binding policyPurchasedBinding() {
        return BindingBuilder
                .bind(policyPurchasedQueue())
                .to(notificationExchange())
                .with(KEY_POLICY_PURCHASED);
    }

    // ── Premium Paid Queue ─────────────────────────────────

    @Bean
    public Queue premiumPaidQueue() {
        return QueueBuilder
                .durable(QUEUE_PREMIUM_PAID)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.premium.paid")
                .build();
    }

    @Bean
    public Binding premiumPaidBinding() {
        return BindingBuilder
                .bind(premiumPaidQueue())
                .to(notificationExchange())
                .with(KEY_PREMIUM_PAID);
    }

    // ── Policy Cancelled Queue ─────────────────────────────

    @Bean
    public Queue policyCancelledQueue() {
        return QueueBuilder
                .durable(QUEUE_POLICY_CANCELLED)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.policy.cancelled")
                .build();
    }

    @Bean
    public Binding policyCancelledBinding() {
        return BindingBuilder
                .bind(policyCancelledQueue())
                .to(notificationExchange())
                .with(KEY_POLICY_CANCELLED);
    }

    // ── JSON Message Converter ─────────────────────────────
    // Serializes Java objects to JSON when publishing to queue
    // Deserializes JSON back to Java objects when consuming

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // ── Premium Due Reminder Queue ─────────────────────────

    @Bean
    public Queue premiumDueReminderQueue() {
        return QueueBuilder
                .durable(QUEUE_PREMIUM_DUE_REMINDER)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.premium.due.reminder")
                .build();
    }

    @Bean
    public Binding premiumDueReminderBinding() {
        return BindingBuilder
                .bind(premiumDueReminderQueue())
                .to(notificationExchange())
                .with(KEY_PREMIUM_DUE_REMINDER);
    }

    // ── Policy Expiry Reminder Queue ───────────────────────

    @Bean
    public Queue policyExpiryReminderQueue() {
        return QueueBuilder
                .durable(QUEUE_POLICY_EXPIRY_REMINDER)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.policy.expiry.reminder")
                .build();
    }

    @Bean
    public Binding policyExpiryReminderBinding() {
        return BindingBuilder
                .bind(policyExpiryReminderQueue())
                .to(notificationExchange())
                .with(KEY_POLICY_EXPIRY_REMINDER);
    }
}