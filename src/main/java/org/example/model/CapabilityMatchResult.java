package org.example.model;

import java.util.List;

/**
 * Structured output produced by the AI capability-matching service.
 * LangChain4j will automatically instruct the model to return JSON matching
 * this shape and parse it back into this record.
 *
 * @param score                 match score out of 10 (can be decimal, e.g. 7.5)
 * @param matchedTechnologies   technologies required by the job that the freelancer already knows
 * @param missingTechnologies   technologies required by the job that the freelancer does NOT appear to know
 * @param reasoning             short explanation of how the score was derived
 */
public record CapabilityMatchResult(
        double score,
        List<String> matchedTechnologies,
        List<String> missingTechnologies,
        String reasoning
) {
    private static final double MATCH_THRESHOLD = 6.9;

    public boolean isMatched() {
        return score > MATCH_THRESHOLD;
    }
}

