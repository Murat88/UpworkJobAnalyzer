package org.example.service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.example.aiservices.services.CapabilityMatchAiService;
import org.example.aiservices.services.ClientEvaluationAiService;
import org.example.aiservices.services.CoverLetterAiService;
import org.example.aiservices.services.JobPostExtractionAiService;
import org.example.model.ApplicationDecision;
import org.example.model.CapabilityMatchResult;
import org.example.model.ClientEvaluationResult;
import org.example.model.JobPost;

/**
 * Orchestrates the full "should I apply to this Upwork job?" pipeline:
 *
 * 0. (Optional) AI extraction of the raw, copy-pasted Upwork job page text into a structured JobPost.
 * 1. AI-based client history / job-post evaluation, driven by ClientEvaluationRules.md.
 * 2. AI-based capability match between the freelancer's profile and the job's tech stack.
 * 3. If both pass, AI-generated cover letter following LetterRules.md.
 */
public class JobApplicationDecisionService {

    private static final String NOT_APPLY_SUMMARY_PREFIX =
            "No, you should NOT apply to this job post. ";
    private static final String APPLY_SUMMARY =
            "Yes, you should apply to this job post. It is a good opportunity for you and fits with your expertise.";

    private final ClientEvaluationAiService clientEvaluationAiService;
    private final CapabilityMatchAiService capabilityMatchAiService;
    private final CoverLetterAiService coverLetterAiService;
    private final JobPostExtractionAiService jobPostExtractionAiService;
    private final FreelancerProfileLoader freelancerProfileLoader;

    public JobApplicationDecisionService(ChatModel model) {
        this.clientEvaluationAiService = AiServices.create(ClientEvaluationAiService.class, model);
        this.capabilityMatchAiService = AiServices.create(CapabilityMatchAiService.class, model);
        this.coverLetterAiService = AiServices.create(CoverLetterAiService.class, model);
        this.jobPostExtractionAiService = AiServices.create(JobPostExtractionAiService.class, model);
        this.freelancerProfileLoader = new FreelancerProfileLoader();
    }

    /**
     * Entry point for the freelancer's actual workflow: paste the raw Upwork job
     * posting page text (job details + "About the client" section) as-is. The AI
     * extracts the structured {@link JobPost}/{@code ClientInfo} first, then the
     * normal decision pipeline runs, using the original raw text for Step 1 so
     * job-post-level rules (e.g. required English level) can be checked too.
     */
    public ApplicationDecision decideFromRawText(String rawJobPostText) {
        JobPost jobPost = jobPostExtractionAiService.extract(rawJobPostText);
        return decideInternal(jobPost, rawJobPostText);
    }

    public ApplicationDecision decide(JobPost jobPost) {
        return decideInternal(jobPost, buildJobDescriptionText(jobPost));
    }

    private ApplicationDecision decideInternal(JobPost jobPost, String jobPostTextForClientEval) {
        // Step 1: client history + job-post-level rules (AI, driven by ClientEvaluationRules.md)
        Double avgHourlyRate = jobPost.client().averageHourlyRatePaidUsd();
        String avgHourlyRateText = avgHourlyRate == null
                ? "not available / not displayed by Upwork for this client"
                : "$%.2f".formatted(avgHourlyRate);

        ClientEvaluationResult clientEvaluationResult = clientEvaluationAiService.evaluate(
                jobPost.client().hireRatePercentage(),
                jobPost.client().paymentMethodVerified(),
                jobPost.client().totalSpentUsd(),
                avgHourlyRateText,
                jobPostTextForClientEval
        );
        if (!clientEvaluationResult.passed()) {
            String summary = NOT_APPLY_SUMMARY_PREFIX + "Client history did not pass evaluation: "
                    + String.join("; ", clientEvaluationResult.failedReasons());
            return new ApplicationDecision(false, summary, clientEvaluationResult, null, null);
        }


        // Step 2: capability match (AI)
        String freelancerProfile = freelancerProfileLoader.load();
        String jobDescription = buildJobDescriptionText(jobPost);
        CapabilityMatchResult capabilityMatchResult =
                capabilityMatchAiService.evaluate(freelancerProfile, jobDescription);

        if (!capabilityMatchResult.isMatched()) {
            String summary = NOT_APPLY_SUMMARY_PREFIX
                    + "Capability score (%.1f/10) is not high enough. Missing: %s"
                            .formatted(capabilityMatchResult.score(),
                                    String.join(", ", capabilityMatchResult.missingTechnologies()));
            return new ApplicationDecision(false, summary, clientEvaluationResult, capabilityMatchResult, null);
        }

        // Both steps passed -> generate the cover letter
        String coverLetter = coverLetterAiService.write(freelancerProfile, jobDescription);

        return new ApplicationDecision(true, APPLY_SUMMARY, clientEvaluationResult, capabilityMatchResult, coverLetter);
    }

    private String buildJobDescriptionText(JobPost jobPost) {
        return """
                Title: %s

                Description:
                %s

                Required tech stack: %s
                """.formatted(jobPost.title(), jobPost.description(), String.join(", ", jobPost.requiredTechStack()));
    }
}

