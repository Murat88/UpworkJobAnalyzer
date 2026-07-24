package org.example.aiservices.services;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * AI service that writes an Upwork cover letter (proposal) for a job post.
 * The writing rules/style guide are kept out of the code, in
 * {@code src/main/resources/LetterRules.md}, so they can be tuned without
 * touching Java code.
 */
public interface CoverLetterAiService {

    @SystemMessage(fromResource = "LetterRules.md")
    @UserMessage("""
            Write an Upwork cover letter (proposal) for the freelancer below,
            applying to the job post below. Follow the rules from the system
            message strictly.

            Freelancer's profile (resume/CV/LinkedIn summary):
            ---
            {{freelancerProfile}}
            ---

            Job post:
            ---
            {{jobDescription}}
            ---
            """)
    String write(@V("freelancerProfile") String freelancerProfile,
                 @V("jobDescription") String jobDescription);
}

