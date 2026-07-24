package org.example.aiservices.services;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.example.model.CapabilityMatchResult;

/**
 * AI service that compares a freelancer's profile (CV/resume/LinkedIn summary)
 * against a job's required tech stack/description, and returns a structured score.
 *
 * LangChain4j automatically instructs the model to answer in JSON matching
 * {@link CapabilityMatchResult} and parses the response for us.
 */
public interface CapabilityMatchAiService {

    @UserMessage("""
            You are an expert technical recruiter evaluating whether a freelancer
            is technically qualified for a job post.

            Freelancer's profile (resume/CV/LinkedIn summary):
            ---
            {{freelancerProfile}}
            ---

            Job post (title, description and/or required tech stack):
            ---
            {{jobDescription}}
            ---

            Compare the technologies, tools and experience the freelancer has
            against what the job requires.

            Return:
            - score: a match score out of 10 (decimals allowed, e.g. 7.5). Consider
              seniority/depth of experience, not just keyword presence.
            - matchedTechnologies: technologies/skills required by the job that the
              freelancer already has experience with.
            - missingTechnologies: technologies/skills required by the job that the
              freelancer does not appear to know.
            - reasoning: a short (2-4 sentence) explanation of the score.
            """)
    CapabilityMatchResult evaluate(@V("freelancerProfile") String freelancerProfile,
                                    @V("jobDescription") String jobDescription);
}

