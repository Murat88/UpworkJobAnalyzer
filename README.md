# Upwork Job Analyzer & Cover Letter Writer 🤖

A Java application built with **[LangChain4j](https://github.com/langchain4j/langchain4j)** and **OpenAI (GPT-4o-mini)** that automates the tedious part of freelancing on Upwork: deciding whether a job post is worth applying to, and — if so — writing a tailored cover letter for it.

Paste the raw text of an Upwork job posting page into a simple web UI, and the app will:

1. **Extract** structured data (title, description, tech stack, client stats) from the raw pasted text using an AI extraction service.
2. **Evaluate the client & job post** against a configurable rule set (hire rate, payment verification, total spend, English level requirements, proposal count, posting age, etc.).
3. **Score the technical fit** between your freelancer profile/resume and the job's required tech stack.
4. **Generate a ready-to-send cover letter** (only if both checks above pass), following a configurable writing style guide.

All of this runs through a minimal embedded HTTP server with zero web framework dependencies — just the JDK's built-in `HttpServer`.

---

## ✨ Features

- **Multi-step AI pipeline** orchestrated in plain Java (`JobApplicationDecisionService`), not a black-box agent — every step is inspectable and independently testable.
- **Structured outputs**: LangChain4j automatically parses the model's JSON responses into typed Java `record`s (`JobPost`, `ClientEvaluationResult`, `CapabilityMatchResult`, `ApplicationDecision`) — no manual JSON parsing of AI responses.
- **Rules kept out of code**: business rules for client evaluation (`ClientEvaluationRules.md`) and cover-letter writing style (`LetterRules.md`) live in Markdown files on the classpath, loaded as system prompts. Tune the AI's behavior without touching Java or recompiling.
- **Single-page UI**: a dependency-free HTML/JS front end (`static/index.html`) for pasting job posts and viewing results.
- **No hardcoded secrets**: the OpenAI API key is read from an environment variable (or JVM system property), never committed to source.

## 🏗️ How it works

```
Raw Upwork job post text (pasted by user)
            │
            ▼
 JobPostExtractionAiService   →  structured JobPost (title, description, tech stack, client stats)
            │
            ▼
 ClientEvaluationAiService    →  passes client history & job-post rules? (ClientEvaluationRules.md)
            │  (fail → stop, "should not apply")
            ▼
 CapabilityMatchAiService     →  score of freelancer profile vs. required tech stack
            │  (low score → stop, "should not apply")
            ▼
 CoverLetterAiService         →  generated cover letter (LetterRules.md)
            │
            ▼
       ApplicationDecision (JSON) → rendered in the web UI
```

Each AI step is a plain Java **interface** annotated with LangChain4j's declarative `@SystemMessage` / `@UserMessage` / `@V` annotations, wired up via `AiServices.create(...)`.

## 📁 Project structure

```
src/main/java/org/example/
├── Main.java                          # Entry point — builds the OpenAiChatModel and starts the web server
├── web/
│   └── WebServer.java                 # Minimal com.sun.net.httpserver-based HTTP server (GET / , POST /evaluate)
├── service/
│   ├── JobApplicationDecisionService.java  # Orchestrates the full decision pipeline
│   └── FreelancerProfileLoader.java   # Loads freelancer-profile.md from the classpath
├── aiservices/services/
│   ├── JobPostExtractionAiService.java   # Raw text → structured JobPost
│   ├── ClientEvaluationAiService.java    # Client/job rules check → ClientEvaluationResult
│   ├── CapabilityMatchAiService.java     # Tech-stack match scoring → CapabilityMatchResult
│   ├── CoverLetterAiService.java         # Cover letter generation
│   ├── AssistantAiService.java           # Generic chat example
│   └── ChefAiService.java                # Simple system-message example (chicken-cooking chef)
└── model/
    ├── JobPost.java                   # title, description, requiredTechStack, client
    ├── ClientInfo.java                # hireRatePercentage, paymentMethodVerified, totalSpentUsd, averageHourlyRatePaidUsd
    ├── ClientEvaluationResult.java    # passed, failedReasons
    ├── CapabilityMatchResult.java     # score, matchedTechnologies, missingTechnologies, reasoning
    └── ApplicationDecision.java       # shouldApply, summary, clientEvaluationResult, capabilityMatchResult, coverLetter

src/main/resources/
├── ClientEvaluationRules.md   # Editable business rules for Step 1 (client & job post evaluation)
├── LetterRules.md             # Editable style guide for the generated cover letter
├── freelancer-profile.md      # Your resume/CV/LinkedIn summary, used for the capability match & cover letter
└── static/index.html          # Single-page front end (paste job post → see decision)
```

## 🛠️ Tech stack

| Component        | Choice                                             |
|------------------|-----------------------------------------------------|
| Language         | Java 21                                             |
| AI orchestration | [LangChain4j](https://github.com/langchain4j/langchain4j) 1.17.0 |
| LLM provider     | OpenAI (`gpt-4o-mini`) via `langchain4j-open-ai`    |
| Web server       | JDK built-in `com.sun.net.httpserver.HttpServer` (no framework) |
| Build tool       | Maven                                               |
| Front end        | Plain HTML/CSS/JS (no build step, no npm)           |

## ✅ Prerequisites

- **JDK 21+**
- **Maven 3.8+**
- An **OpenAI API key** with access to `gpt-4o-mini` (or another model — see [Configuration](#-configuration))

## 🚀 Getting started

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/UpworkJobAnalyzer.git
cd UpworkJobAnalyzer
```

### 2. Set your OpenAI API key

The key is **never** hardcoded. Set it as an environment variable:

```bash
export OPENAI_API_KEY="sk-..."
```

Alternatively, pass it as a JVM system property when running the app:

```bash
-Dopenai.api.key=sk-...
```

### 3. (Optional) Personalize your freelancer profile

Replace the contents of `src/main/resources/freelancer-profile.md` with your own resume/CV/LinkedIn summary so the capability match and cover letter are generated for **your** skills, not the sample profile included here.

### 4. Build & run

```bash
mvn clean package
mvn exec:java -Dexec.mainClass="org.example.Main"
```

or simply run `Main.java` from your IDE.

You should see:

```
Upwork Job Analyzer is running: http://localhost:8090
```

### 5. Use the app

Open [http://localhost:8090](http://localhost:8090) in your browser, paste the raw text of an Upwork job posting page (job details + "About the client" section), and click **Evaluate**.

## ⚙️ Configuration

You can tune the AI's behavior **without touching any Java code**:

- **`src/main/resources/ClientEvaluationRules.md`** — the rules used to decide whether a client/job post passes Step 1 (e.g. minimum hire rate, minimum total spend, required English level, max proposal count, max job age). Add, remove, or edit rules freely; the AI reads the whole file as its system prompt.
- **`src/main/resources/LetterRules.md`** — the style guide followed when generating the cover letter (tone, length, structure, do's and don'ts).
- **`src/main/resources/freelancer-profile.md`** — your profile used for the capability-match scoring and cover letter generation.
- **`Main.java`** — change the model (`OpenAiChatModelName`) or port (`PORT`) as needed.

## 🌐 API

| Method | Path        | Description                                                                 |
|--------|-------------|-------------------------------------------------------------------------------|
| GET    | `/`         | Serves the single-page UI                                                     |
| POST   | `/evaluate` | Body = raw pasted job post text. Response = JSON `ApplicationDecision`         |

Example response:

```json
{
  "shouldApply": true,
  "summary": "Yes, you should apply to this job post...",
  "clientEvaluation": { "passed": true, "failedReasons": [] },
  "capabilityMatch": {
    "score": 8.5,
    "matchedTechnologies": ["Java", "Spring Boot"],
    "missingTechnologies": ["Kubernetes"],
    "reasoning": "..."
  },
  "coverLetter": "..."
}
```

## 🔒 Security notes

- The OpenAI API key is read exclusively from the `OPENAI_API_KEY` environment variable (or the `openai.api.key` system property) at startup — it is **never** committed to source control.
- Make sure your own `freelancer-profile.md` doesn't contain sensitive information you don't want sent to the OpenAI API, since it's included in every prompt.

## 📄 License

This project is provided as-is for personal/educational use. Feel free to fork and adapt it to your own freelancing workflow.

