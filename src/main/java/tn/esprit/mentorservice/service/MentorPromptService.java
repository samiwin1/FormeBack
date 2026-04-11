package tn.esprit.mentorservice.service;

import org.springframework.stereotype.Service;
import tn.esprit.mentorservice.dto.LearningPathContext;

@Service
public class MentorPromptService {

    private static final String EXTENDED_PREFS_HINT = """
            If the portfolio JSON includes "extendedPreferences", respect those learner choices
            (certification goals, exam style, schedule, device/accessibility, motivation tone, depth vs breadth,
            explanation style, resource languages, linked formation ids) unless facts are missing—then say what is missing.
            """;

    private static final String JSON_SCHEMA_HINT = """
            Respond with ONLY valid JSON (no markdown fences) using this shape:
            {
              "summary": "one short paragraph",
              "bullets": ["short point 1", "short point 2"],
              "weakTopics": ["optional topic tags"],
              "suggestedActions": ["concrete next steps"],
              "studyPlan": {
                "title": "optional plan title",
                "items": [
                  { "type": "READ|QUIZ|REVIEW|LAB|WATCH", "title": "step", "durationMinutes": 30, "resourceRef": "optional" }
                ]
              }
            }
            """;

    private static final String ASK_JSON_SCHEMA_HINT = """
            Respond with ONLY valid JSON (no markdown fences) using this shape:
            {
              "type": "FORMATION_RECOMMENDATION | FORMATION_MISSING | GENERAL_ADVICE",
              "summary": "one short paragraph",
              "bullets": ["short point 1", "short point 2"],
              "weakTopics": ["optional topic tags"],
              "suggestedActions": ["concrete next steps"],
              "recommendedFormationIds": [1],
              "recommendedCertificationIds": [2],
              "recommendationNotes": "brief note on why these fit the learner (optional)",
              "detectedTargetRole": "Backend Developer",
              "detectedSkillsGap": "Spring Boot, REST APIs, SQL",
              "studyPlan": {
                "title": "optional plan title",
                "items": [
                  { "type": "READ|QUIZ|REVIEW|LAB|WATCH", "title": "step", "durationMinutes": 30, "resourceRef": "optional" }
                ]
              }
            }
            Rules for the "type" field (REQUIRED — never omit):
            - Set type = "FORMATION_RECOMMENDATION" if the user is asking about a topic, skill, technology, or career path AND at least one formation in the catalog matches. Populate recommendedFormationIds with the matching ids.
            - Set type = "FORMATION_MISSING" if the user is asking for a formation, course, or learning resource on a topic/skill/technology/role AND NO formation in the catalog matches it. This includes questions like "give me formation for X", "I want to learn X", "is there a course on X", or "how to become X". Do NOT invent ids — leave recommendedFormationIds as [].
            - Set type = "GENERAL_ADVICE" for all other questions (general advice, exam help, study tips — no formation request detected).
            IMPORTANT — when type is "FORMATION_MISSING":
            - The summary MUST be encouraging and forward-looking. Say that a formation on [topic] is coming soon and that the learner's interest has been noted. NEVER just say "we don't have X". Example: "A formation on Cloud Technologies is in the works! We've noted your interest and will make it available soon. In the meantime, here are some self-study resources to get started."
            - detectedTargetRole: set to the topic, skill, technology, or job role the learner asked about (e.g. "Cloud Technologies", "DevOps Engineer", "Kubernetes"). Set to null only if truly nothing specific was requested.
            - detectedSkillsGap: comma-separated prerequisite skills for that topic/role that the learner is missing based on their portfolio. Set to null if unknown.
            Rules for other fields:
            - detectedTargetRole: for non-FORMATION_MISSING types, extract the job role or career goal from the question. Set to null if no role is expressed.
            - detectedSkillsGap: comma-separated list of skills needed for that role that the learner currently lacks based on their portfolio. Set to null if no role is detected.
            - recommendedFormationIds: only use numeric ids that appear in the "Published formations catalog" JSON array (field id). If none fit, use [].
            - recommendedCertificationIds: only use ids from publishedCertifications in the certification context JSON. If missing or none fit, use [].
            - Do not invent formation or certification ids.
            """;

    public String buildAskPrompt(
            String locale,
            String portfolioJson,
            String progressJson,
            String formationCatalogJson,
            String certificationContextJson,
            String priorConversationBlock,
            String question,
            Long formationId,
            String feedbackContext   // nullable — injected when feedback history exists
    ) {
        String lang = locale != null && !locale.isBlank() ? locale : "en";
        String formationLine = formationId != null
                ? "Optional formation focus (id=" + formationId + ").\n"
                : "";

        String historySection = priorConversationBlock == null || priorConversationBlock.isBlank()
                ? "Prior conversation in this chat: none.\n"
                : "Prior conversation in this chat:\n" + priorConversationBlock + "\n";

        String feedbackSection = (feedbackContext != null && !feedbackContext.isBlank())
                ? feedbackContext + "\n"
                : "";

        return """
                You are a supportive learning mentor for an online training platform.
                Base your answer ONLY on the learner context JSON below plus the learner question and prior conversation.
                If data is missing, say what is missing instead of inventing scores or enrollments.
                Language: respond in %s when possible.

                %s

                %s

                %s

                %s

                Learner portfolio (JSON):
                %s

                Recent progress snapshot from formation-service (JSON, may be null):
                %s

                Published formations catalog from formation-service (JSON array; use these ids for formation recommendations):
                %s

                Certification-service context (JSON; exam/cert status and published certification catalog—use certification ids only from publishedCertifications):
                %s

                Learner question:
                %s

                %s
                """.formatted(
                lang,
                formationLine,
                EXTENDED_PREFS_HINT,
                historySection,
                feedbackSection,
                portfolioJson,
                progressJson,
                formationCatalogJson,
                certificationContextJson,
                question,
                ASK_JSON_SCHEMA_HINT
        );
    }

    public String buildWeeklyBriefPrompt(String locale, String portfolioJson, String progressJson) {
        String lang = locale != null && !locale.isBlank() ? locale : "en";
        return """
                You are a learning coach. Write a concise weekly brief for the learner.
                Use ONLY the JSON facts below. Highlight progress, one risk, and 3 prioritized actions.
                Language: %s

                %s

                Portfolio JSON:
                %s

                Progress snapshot JSON:
                %s

                %s
                """.formatted(lang, EXTENDED_PREFS_HINT, portfolioJson, progressJson, JSON_SCHEMA_HINT);
    }

    public String buildPreExamTipsPrompt(
            String locale,
            String portfolioJson,
            String progressJson,
            long formationId
    ) {
        String lang = locale != null && !locale.isBlank() ? locale : "en";
        return """
                You are a learning coach. The learner is preparing for the final exam of formation id=%d.
                Give at most 5 prioritized, practical tips. Use ONLY the JSON data; do not invent scores.
                If there is no attempt data, give general exam prep aligned to their stated goals and skills.
                Language: %s

                %s

                Portfolio JSON:
                %s

                Progress snapshot for this formation (JSON):
                %s

                %s
                """.formatted(formationId, lang, EXTENDED_PREFS_HINT, portfolioJson, progressJson, JSON_SCHEMA_HINT);
    }

    private static final String LEARNING_PATH_JSON_SCHEMA = """
            Respond with ONLY valid JSON (no markdown fences) using this exact shape:
            {
              "summary": "one short paragraph describing the overall recommended path",
              "totalEstimatedWeeks": 12,
              "formations": [
                {
                  "formationId": 5,
                  "title": "Formation title",
                  "priority": 1,
                  "reason": "Why this formation matters for the learner's goals",
                  "estimatedWeeks": 3,
                  "prerequisiteMet": true,
                  "status": "NOT_STARTED | IN_PROGRESS | COMPLETED"
                }
              ],
              "certificationReadiness": [
                {
                  "certificationName": "AWS SAA",
                  "targetDate": "2026-09-01",
                  "onTrack": false,
                  "remainingFormations": 3,
                  "riskLevel": "HIGH | MEDIUM | LOW",
                  "recommendation": "Concrete action to get back on track"
                }
              ],
              "skillGaps": [
                {
                  "skillName": "Terraform",
                  "currentLevel": "NONE | LOW | MEDIUM | HIGH",
                  "targetLevel": "LOW | MEDIUM | HIGH",
                  "suggestion": "Self-study recommendation if no formation covers it"
                }
              ],
              "nextAction": "The single most important thing the learner should do right now"
            }
            Rules:
            - formations[].formationId must come from the "Available formations catalog" JSON (field id). Do not invent ids.
            - Only list formations relevant to the learner's goals. Sort by priority ascending (1 = do first).
            - certificationReadiness: only include certifications listed in extendedPreferences.targetCertifications.
            - skillGaps: only flag skills needed for the learner's targetRole or targetCertifications that are not covered by any available formation.
            - estimatedWeeks: calculate from formation estimated duration and learner weeklyStudyHours. If unknown, estimate conservatively.
            - If a data source was unavailable, note it briefly in the summary but still provide best-effort recommendations.
            """;

    public String buildRemediationPrompt(
            String preferredLanguage,
            String portfolioJson,
            String progressJson,
            String alertDescription
    ) {
        String lang = preferredLanguage != null && !preferredLanguage.isBlank() ? preferredLanguage : "en";
        return """
                You are an empathetic learning coach helping a learner who is struggling.
                Generate a concise, targeted remediation plan (3-5 focused actions).
                Focus ONLY on the formation where the learner is having difficulty.
                Use the learner's preferred learning style to explain difficult concepts differently:
                  - VISUAL → use analogies, comparisons, and mental models
                  - HANDS_ON → suggest practical exercises and projects
                  - READING → provide detailed step-by-step explanations
                  - VIDEO → suggest walkthrough-style learning sequences
                Be encouraging — acknowledge the learner's effort and normalise struggling.
                Do NOT invent scores or enrollments not present in the data.
                Language: respond in %s when possible.

                %s

                Difficulty summary: %s

                Learner portfolio (JSON):
                %s

                Progress snapshot for this formation (JSON):
                %s

                %s
                """.formatted(lang, EXTENDED_PREFS_HINT, alertDescription, portfolioJson, progressJson, JSON_SCHEMA_HINT);
    }

    public String buildLearningPathPrompt(LearningPathContext ctx) {
        String lang = "en";
        // extract preferredLanguage from portfolioJson best-effort (prompt-level override)
        if (ctx.portfolioJson() != null && ctx.portfolioJson().contains("\"preferredLanguage\"")) {
            int idx = ctx.portfolioJson().indexOf("\"preferredLanguage\"");
            int colon = ctx.portfolioJson().indexOf(':', idx);
            int quote1 = ctx.portfolioJson().indexOf('"', colon + 1);
            int quote2 = ctx.portfolioJson().indexOf('"', quote1 + 1);
            if (quote1 >= 0 && quote2 > quote1) {
                String extracted = ctx.portfolioJson().substring(quote1 + 1, quote2).trim();
                if (!extracted.isBlank()) {
                    lang = extracted;
                }
            }
        }

        String unavailableNote = ctx.unavailableSources().isEmpty()
                ? ""
                : "Note: the following data sources were unavailable — " +
                  String.join(", ", ctx.unavailableSources()) +
                  ". Make your best-effort recommendations based on what is available.\n";

        return """
                You are a strategic learning coach. Generate a personalised, prioritised learning path for the learner.
                Analyse their current state (completed formations, earned certifications, self-assessed skills) and
                recommend what they should learn next to reach their target role and certification goals.
                Use ONLY the data provided below. Do not invent formation ids, certification ids, or scores.
                Language: respond in %s when possible.

                %s

                %s

                Learner portfolio (JSON):
                %s

                Available formations catalog from formation-service (JSON array):
                %s

                Learner progress snapshot from formation-service (JSON, may be null if unavailable):
                %s

                Certification-service context — earned certs, exam status, published catalog (JSON, may be null):
                %s

                %s
                """.formatted(
                lang,
                unavailableNote,
                EXTENDED_PREFS_HINT,
                ctx.portfolioJson(),
                ctx.formationCatalogJson(),
                ctx.progressJson(),
                ctx.certificationContextJson(),
                LEARNING_PATH_JSON_SCHEMA
        );
    }
}
