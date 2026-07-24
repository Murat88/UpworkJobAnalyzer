package org.example.model;

/**
 * Final outcome of the whole pipeline: client evaluation + capability match + (optional) cover letter.
 */
public record ApplicationDecision(
        boolean shouldApply,
        String summary,
        ClientEvaluationResult clientEvaluationResult,
        CapabilityMatchResult capabilityMatchResult,
        String coverLetter
) {
}

