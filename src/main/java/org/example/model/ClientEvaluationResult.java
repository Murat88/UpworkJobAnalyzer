package org.example.model;

import java.util.List;

/**
 * Result of the deterministic (rule-based, non-AI) client history evaluation.
 */
public record ClientEvaluationResult(
        boolean passed,
        List<String> failedReasons
) {
    public static ClientEvaluationResult ok() {
        return new ClientEvaluationResult(true, List.of());
    }

    public static ClientEvaluationResult failed(List<String> reasons) {
        return new ClientEvaluationResult(false, reasons);
    }
}

