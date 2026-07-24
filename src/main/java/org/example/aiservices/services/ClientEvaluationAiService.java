package org.example.aiservices.services;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.example.model.ClientEvaluationResult;

/**
 * AI service that evaluates a client's history and job-post-level requirements
 * (e.g. required English level) against the rules defined in
 * {@code src/main/resources/ClientEvaluationRules.md}.
 *
 * Rules are kept out of the code entirely so they can be added/edited without
 * touching Java: LangChain4j loads that file as the system message, and
 * automatically parses the model's JSON response into {@link ClientEvaluationResult}.
 */
public interface ClientEvaluationAiService {

    @SystemMessage(fromResource = "ClientEvaluationRules.md")
    @UserMessage("""
            Evaluate the following client and job post against the rules in your
            system message.

            Client info:
            - Hire rate: {{hireRatePercentage}}%
            - Payment method verified: {{paymentMethodVerified}}
            - Total spent: ${{totalSpentUsd}}
            - Average hourly rate paid: {{averageHourlyRatePaidUsd}}

            Job post text (use this to check any job-related rules, such as a
            required English level mentioned under "Preferred qualifications"
            or similar sections):
            ---
            {{jobPostText}}
            ---
            """)
    ClientEvaluationResult evaluate(@V("hireRatePercentage") double hireRatePercentage,
                                     @V("paymentMethodVerified") boolean paymentMethodVerified,
                                     @V("totalSpentUsd") double totalSpentUsd,
                                     @V("averageHourlyRatePaidUsd") String averageHourlyRatePaidUsd,
                                     @V("jobPostText") String jobPostText);
}



