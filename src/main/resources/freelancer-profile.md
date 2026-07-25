# Freelancer Profile

> **📌 Instructions for end users**
>
> This file is used by the AI services (e.g. `CoverLetterAiService`, `CapabilityMatchAiService`,
> `ClientEvaluationAiService`) as the source of truth for **your** professional background. The
> application reads this file at runtime via `FreelancerProfileLoader`
> (`src/main/java/org/example/service/FreelancerProfileLoader.java`) and injects its full content
> into the AI prompts, so the more complete and accurate the information below is, the better the
> generated cover letters, capability matches, and client evaluations will be.
>
> **Replace every section below with your own information.** Do not commit your real resume to a
> public repository — either:
> 1. Edit this file locally with your own details and add it to `.gitignore` before committing, or
> 2. Keep this template in version control and load a private copy at runtime by calling
     >    `new FreelancerProfileLoader().load("your-private-file.md")` with a file that is excluded from
     >    Git.
>
> Delete this instructions block once you've filled in your own profile.

---

## Professional Summary
[2-4 sentences summarizing your years of experience, core domain(s), key technical strengths, and
any leadership/management experience. Example: "Software professional with X years of experience
in [domain], including Y years in a [role] position. Proven track record in [key strengths]."]

## Technical Skills
- **Programming Languages:** [e.g. Java, C#, Python, JavaScript, TypeScript]
- **Backend Technologies:** [e.g. Spring Boot, ASP.NET, Node.js, etc.]
- **Databases & Integration:** [e.g. PostgreSQL, MySQL, MongoDB, Kafka, REST/SOAP integrations]
- **DevOps & CI/CD:** [e.g. Docker, Kubernetes, Jenkins, GitHub Actions]
- **Version Control:** [e.g. Git, GitHub, GitLab, Bitbucket]
- **Project & Issue Management:** [e.g. JIRA, Trello, Azure DevOps]

## Experience
### [Company Name] — [Role] ([Start Date] - [End Date / Present])
- [Key responsibility or achievement #1]
- [Key responsibility or achievement #2]
- [Key responsibility or achievement #3]

### [Company Name] — [Role] ([Start Date] - [End Date])
- [Key responsibility or achievement #1]
- [Key responsibility or achievement #2]

<!-- Repeat the block above for each previous position, most recent first. -->

## Education
**[University Name]** — [Degree, Field of Study] ([Start Year] - [End Year])

## Courses & Certifications
- [Course/Certificate name — Issuing organization]
- [Course/Certificate name — Issuing organization]

## Languages
- [Language] (Native/Fluent/Professional/Basic)
- [Language] (Native/Fluent/Professional/Basic)

---

## Detailed Project / Key Contributions (optional but recommended)

Providing more granular detail per role significantly improves AI-generated cover letters and
capability-match results. For each role, consider adding a "Key Contributions" section, for
example:

### [Company Name] — [Role] ([Years])
**Key Contributions**

- **[Area/Category, e.g. "Backend Architecture"]**
  - [Detail describing what you built, the tech used, and the impact/scale]
- **[Area/Category, e.g. "Team Leadership"]**
  - [Detail describing your leadership scope and outcomes]

<!-- Add as many detailed sections as needed. -->
