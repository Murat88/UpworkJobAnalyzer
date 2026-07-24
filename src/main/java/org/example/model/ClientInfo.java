package org.example.model;

/**
 * Represents an Upwork client's history/statistics, as visible on their profile.
 *
 * @param hireRatePercentage       percentage (0-100) of job posts that resulted in a hire
 * @param paymentMethodVerified    whether the client's payment method is verified
 * @param totalSpentUsd            total amount (USD) the client has spent on the platform
 * @param averageHourlyRatePaidUsd average hourly rate the client has historically paid, if known/displayed (nullable)
 */
public record ClientInfo(
        Double hireRatePercentage,
        Boolean paymentMethodVerified,
        Double totalSpentUsd,
        Double averageHourlyRatePaidUsd
) {
    /**
     * Compact constructor: the AI extraction step may return {@code null} for
     * fields it couldn't find in the raw text. For {@code hireRatePercentage},
     * {@code paymentMethodVerified} and {@code totalSpentUsd}, treat a missing
     * value as the safest/most conservative default (0 / false) so the rest of
     * the pipeline never has to null-check them.
     *
     * {@code averageHourlyRatePaidUsd} is intentionally left {@code null} as-is
     * when missing: its absence has a specific meaning ("this client has never
     * displayed an average hourly rate"), and {@code ClientEvaluationRules.md}
     * relies on that distinction to skip the rule entirely instead of failing it.
     */
    public ClientInfo {
        if (hireRatePercentage == null) {
            hireRatePercentage = 0.0;
        }
        if (paymentMethodVerified == null) {
            paymentMethodVerified = false;
        }
        if (totalSpentUsd == null) {
            totalSpentUsd = 0.0;
        }
    }
}


