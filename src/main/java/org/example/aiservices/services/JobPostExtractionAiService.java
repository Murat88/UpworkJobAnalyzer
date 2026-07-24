package org.example.aiservices.services;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.example.model.JobPost;

/**
 * AI service that parses the raw, copy-pasted text of an Upwork job posting
 * page (fixed layout: job details followed by an "About the client" section)
 * into a structured {@link JobPost} (which itself contains a nested
 * {@code ClientInfo}).
 *
 * This lets the freelancer simply paste the whole page instead of manually
 * filling in every field.
 */
public interface JobPostExtractionAiService {

    @SystemMessage("""
            You are a precise data-extraction engine. You will be given the raw text
            copy-pasted directly from an Upwork job posting page. The page always has
            two parts, in this order:

            1. The job details: title (first line), "Posted X ago", location,
               "Summary" (the job description paragraph), engagement type
               (Hourly/Fixed price), hours/week, duration, experience level,
               budget/rate range, project type, "Skills and Expertise" with a
               "Mandatory skills" list (and sometimes "Preferred qualifications"),
               location preferences, and an "Activity on this job" section
               (proposals count, invites, bid range, etc).

            2. An "About the client" section with statistics such as:
               - "Payment method verified" (present) or "Payment method not verified" / absent
               - a star rating and review count
               - client's country/city/local time
               - "NN jobs posted"
               - "NN% hire rate, N open jobs"
               - "$X total spent" (may be abbreviated, e.g. "$1.3K" or "$2.5M")
               - "N hires, N active"
               - "$X /hr avg hourly rate paid" (may be absent if the client never hired hourly)
               - "Member since ..."

            Extract the following fields exactly:

            - title: the job title (first line of the text).
            - description: a concise but complete summary of the job, combining the
              "Summary" paragraph with other important job details you find useful
              context for evaluating fit (engagement type, hours/week, duration,
              experience level, budget/rate range, project type). Do NOT include the
              client statistics here.
            - requiredTechStack: a list of the individual skills/technologies listed
              under "Mandatory skills" (and "Preferred qualifications" too, if it
              lists actual skills rather than just locations). Keep each skill as a
              short separate list item (e.g. "API", "Web Development"), do not merge
              them into one string.
            - client.hireRatePercentage: the number before "% hire rate" (e.g. 42.0
              for "42% hire rate"). If missing, use 0.
            - client.paymentMethodVerified: true only if the text explicitly says
              "Payment method verified"; false otherwise (including if it says
              "not verified" or the phrase is entirely absent).
            - client.totalSpentUsd: the number after "total spent", converted to a
              plain number (e.g. "$1.3K total spent" -> 1300, "$25K" -> 25000,
              "$1.2M" -> 1200000, "$500" -> 500). If missing, use 0.
            - client.averageHourlyRatePaidUsd: the number before "/hr avg hourly rate
              paid" (e.g. 11.42 for "$11.42 /hr avg hourly rate paid"). If this line
              is entirely absent from the text, return null for this field.

            Only use information that is actually present in the text. Never invent
            numbers.
            """)
    @UserMessage("""
            Parse the following raw Upwork job posting text into structured data:

            ---
            {{rawText}}
            ---
            """)
    JobPost extract(@V("rawText") String rawText);
}

