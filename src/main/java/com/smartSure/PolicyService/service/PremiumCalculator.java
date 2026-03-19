package com.smartSure.PolicyService.service;

import com.smartSure.PolicyService.dto.calculation.PremiumCalculationResponse;
import com.smartSure.PolicyService.entity.Policy;
import com.smartSure.PolicyService.entity.PolicyType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class PremiumCalculator {

    private static final BigDecimal BASE_COVERAGE_UNIT = new BigDecimal("100000");

    // ==================== MAIN CALCULATION ====================

    /**
     * Calculate premium per installment including frequency loading.
     */
    public PremiumCalculationResponse calculatePremium(
            PolicyType policyType,
            BigDecimal coverageAmount,
            Policy.PaymentFrequency frequency,
            Integer age
    ) {
        BigDecimal coverageFactor = calculateCoverageFactor(coverageAmount);
        BigDecimal ageFactor = calculateAgeFactor(age);

        BigDecimal annualPremium = calculateAnnual(policyType, coverageAmount, age);
        BigDecimal perInstallment = applyFrequency(annualPremium, frequency);

        return PremiumCalculationResponse.builder()
                .basePremium(policyType.getBasePremium())
                .annualPremium(annualPremium)
                .calculatedPremium(perInstallment)
                .paymentFrequency(frequency.name())
                .breakdown(buildBreakdown(policyType, coverageFactor, ageFactor, frequency))
                .build();
    }

    /**
     * Calculate annual premium without frequency adjustments.
     */
    public BigDecimal calculateAnnual(PolicyType policyType, BigDecimal coverageAmount, Integer age) {
        BigDecimal coverageFactor = calculateCoverageFactor(coverageAmount);
        BigDecimal ageFactor = calculateAgeFactor(age);

        return policyType.getBasePremium()
                .multiply(coverageFactor)
                .multiply(ageFactor)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // ==================== CORE FACTORS ====================

    private BigDecimal calculateCoverageFactor(BigDecimal coverageAmount) {
        return coverageAmount.divide(BASE_COVERAGE_UNIT, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAgeFactor(Integer age) {
        if (age == null) return BigDecimal.ONE;

        if (age < 25) return new BigDecimal("0.85");
        if (age < 35) return BigDecimal.ONE;
        if (age < 45) return new BigDecimal("1.20");
        if (age < 55) return new BigDecimal("1.50");
        if (age < 65) return new BigDecimal("1.90");
        return new BigDecimal("2.50");
    }

    // ==================== FREQUENCY LOGIC ====================

    private BigDecimal applyFrequency(BigDecimal annualPremium, Policy.PaymentFrequency frequency) {
        return switch (frequency) {
            case MONTHLY -> annualPremium.multiply(new BigDecimal("1.05"))
                    .divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
            case QUARTERLY -> annualPremium.multiply(new BigDecimal("1.03"))
                    .divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP);
            case SEMI_ANNUAL -> annualPremium.multiply(new BigDecimal("1.01"))
                    .divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
            case ANNUAL -> annualPremium;
        };
    }

    // ==================== SCHEDULE HELPERS ====================

    /**
     * Number of installments over the policy term.
     */
    public int installmentCount(int termMonths, Policy.PaymentFrequency frequency) {
        return switch (frequency) {
            case MONTHLY -> termMonths;
            case QUARTERLY -> termMonths / 3;
            case SEMI_ANNUAL -> termMonths / 6;
            case ANNUAL -> termMonths / 12;
        };
    }

    /**
     * Months between installments.
     */
    public int monthsBetweenInstallments(Policy.PaymentFrequency frequency) {
        return switch (frequency) {
            case MONTHLY -> 1;
            case QUARTERLY -> 3;
            case SEMI_ANNUAL -> 6;
            case ANNUAL -> 12;
        };
    }

    // ==================== HELPER: SUM ACTIVE COVERAGES ====================

    /**
     * Sum total coverage amount of active policies.
     */
    public BigDecimal sumActiveCoverages(List<Policy> activePolicies) {
        return activePolicies.stream()
                .map(Policy::getCoverageAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ==================== BREAKDOWN (INTERVIEW GOLD) ====================

    private String buildBreakdown(
            PolicyType policyType,
            BigDecimal coverageFactor,
            BigDecimal ageFactor,
            Policy.PaymentFrequency frequency
    ) {
        return "BasePremium=" + policyType.getBasePremium()
                + ", CoverageFactor=" + coverageFactor
                + ", AgeFactor=" + ageFactor
                + ", Frequency=" + frequency.name();
    }
}