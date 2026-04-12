package tn.esprit.formation_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.formation_service.dto.GenerateFormationRequest;
import tn.esprit.formation_service.entity.ContenuFormation;
import tn.esprit.formation_service.entity.Evaluation;
import tn.esprit.formation_service.entity.Examen;
import tn.esprit.formation_service.entity.Formation;
import tn.esprit.formation_service.exception.GeminiApiException;

import java.util.ArrayList;
import java.util.List;

@Service
public class FormationGeneratorServiceImpl implements FormationGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(FormationGeneratorServiceImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final GeminiApiService geminiApiService;
    private final FormationService formationService;
    private final ContenuFormationService contenuFormationService;
    private final EvaluationService evaluationService;
    private final ExamenService examenService;

    public FormationGeneratorServiceImpl(GeminiApiService geminiApiService,
                                        FormationService formationService,
                                        ContenuFormationService contenuFormationService,
                                        EvaluationService evaluationService,
                                        ExamenService examenService) {
        this.geminiApiService = geminiApiService;
        this.formationService = formationService;
        this.contenuFormationService = contenuFormationService;
        this.evaluationService = evaluationService;
        this.examenService = examenService;
    }

    @Override
    @Transactional
    public Formation generateAndSave(GenerateFormationRequest request) {
        int numBlocks = request.getNumberOfContentBlocks() != null && request.getNumberOfContentBlocks() > 0
                ? request.getNumberOfContentBlocks() : 3;

        String json;
        try {
            json = geminiApiService.generateFormationStructure(
                    request.getTitle(),
                    request.getDescription(),
                    request.getObjectives(),
                    request.getLevel(),
                    request.getSkillsTargeted(),
                    numBlocks
            );
        } catch (GeminiApiException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("api key") || msg.contains("expired") || msg.contains("invalid") || msg.contains("403") || msg.contains("401")) {
                log.warn("Gemini API key invalid or expired, using fallback template: {}", e.getMessage());
                json = buildFallbackFormationJson(request, numBlocks);
            } else {
                throw e;
            }
        }

        if (json == null || json.isBlank()) {
            log.warn("AI did not return formation structure");
            return null;
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);

            // 1. Create Formation
            Formation formation = parseAndCreateFormation(root, request.getCreatedBy());
            formation = formationService.save(formation);

            // 2. Create Evaluations (must be saved first to get IDs for content linking)
            List<Evaluation> evaluations = parseAndCreateEvaluations(root, formation);
            List<Evaluation> savedEvals = new ArrayList<>();
            for (Evaluation e : evaluations) {
                savedEvals.add(evaluationService.save(e));
            }

            // 3. Create Content blocks and link to evaluations
            List<ContenuFormation> contents = parseAndCreateContents(root, formation, savedEvals);
            for (ContenuFormation c : contents) {
                contenuFormationService.save(c);
            }

            // 4. Create Exam
            Examen exam = parseAndCreateExam(root, formation, request.getCreatedBy());
            if (exam != null) {
                examenService.save(exam);
            }

            return formation;
        } catch (Exception e) {
            log.error("Failed to parse or save AI-generated formation: {}", e.getMessage());
            throw new RuntimeException("Failed to generate formation: " + e.getMessage());
        }
    }

    private Formation parseAndCreateFormation(JsonNode root, Long createdBy) {
        JsonNode f = root.get("formation");
        if (f == null) {
            throw new IllegalArgumentException("Missing 'formation' in AI response");
        }
        Formation formation = new Formation();
        formation.setTitle(getText(f, "title", "AI Generated Formation"));
        formation.setDescription(getText(f, "description", ""));
        formation.setObjectives(getText(f, "objectives", ""));
        formation.setLevel(getText(f, "level", "intermediate"));
        formation.setSkills_targeted(getText(f, "skills_targeted", ""));
        formation.setStatus("draft");
        formation.setCreated_by(createdBy);
        return formation;
    }

    private List<Evaluation> parseAndCreateEvaluations(JsonNode root, Formation formation) {
        List<Evaluation> list = new ArrayList<>();
        JsonNode evals = root.get("evaluations");
        if (evals == null || !evals.isArray()) {
            return list;
        }
        for (JsonNode e : evals) {
            Evaluation eval = new Evaluation();
            eval.setFormation(formation);
            eval.setTitle(getText(e, "title", "Quiz"));
            eval.setContent(getText(e, "content", "{\"questions\":[]}"));
            eval.setEvaluation_type("quiz");
            eval.setPassing_score(e.has("passing_score") && e.get("passing_score").isNumber() ? e.get("passing_score").asInt() : 75);
            eval.setMax_attempts(e.has("max_attempts") && e.get("max_attempts").isNumber() ? e.get("max_attempts").asInt() : 3);
            list.add(eval);
        }
        return list;
    }

    private List<ContenuFormation> parseAndCreateContents(JsonNode root, Formation formation, List<Evaluation> evaluations) {
        List<ContenuFormation> list = new ArrayList<>();
        JsonNode contents = root.get("contents");
        if (contents == null || !contents.isArray()) {
            return list;
        }
        for (int i = 0; i < contents.size(); i++) {
            JsonNode c = contents.get(i);
            ContenuFormation cf = new ContenuFormation();
            cf.setFormation(formation);
            cf.setTitle(getText(c, "title", "Content Block " + (i + 1)));
            cf.setContent_type(getText(c, "content_type", "text"));
            cf.setContent_body(getText(c, "content_body", ""));
            cf.setOrder_index(c.has("order_index") && c.get("order_index").isNumber() ? c.get("order_index").asInt() : i);
            cf.setIs_locked(false);
            if (i < evaluations.size()) {
                cf.setEvaluation(evaluations.get(i));
            }
            list.add(cf);
        }
        return list;
    }

    private Examen parseAndCreateExam(JsonNode root, Formation formation, Long createdBy) {
        JsonNode ex = root.get("exam");
        if (ex == null) {
            return null;
        }
        Examen exam = new Examen();
        exam.setFormation(formation);
        exam.setTitle(getText(ex, "title", "Final Exam"));
        exam.setDuration_minutes(ex.has("duration_minutes") && ex.get("duration_minutes").isNumber() ? ex.get("duration_minutes").asInt() : 30);
        exam.setPassing_score(ex.has("passing_score") && ex.get("passing_score").isNumber() ? ex.get("passing_score").asInt() : 75);
        exam.setContent(getText(ex, "content", "{\"questions\":[]}"));
        exam.setCreated_by(createdBy);
        return exam;
    }

    private static String getText(JsonNode node, String key, String defaultValue) {
        JsonNode n = node.get(key);
        if (n == null || n.isNull()) {
            return defaultValue;
        }
        return n.asText(defaultValue);
    }

    /**
     * When Gemini API key is expired/invalid, build a minimal formation structure so "Generate with AI" still completes.
     */
    private String buildFallbackFormationJson(GenerateFormationRequest request, int numBlocks) {
        String title = request.getTitle() != null && !request.getTitle().isBlank() ? escapeJson(request.getTitle()) : "Generated Formation";
        String desc = request.getDescription() != null ? escapeJson(request.getDescription()) : "";
        String objectives = request.getObjectives() != null ? escapeJson(request.getObjectives()) : "";
        String level = request.getLevel() != null ? escapeJson(request.getLevel()) : "intermediate";
        String skills = request.getSkillsTargeted() != null ? escapeJson(request.getSkillsTargeted()) : "";

        StringBuilder sb = new StringBuilder();
        sb.append("{\"formation\":{\"title\":\"").append(title).append("\",\"description\":\"").append(desc)
          .append("\",\"objectives\":\"").append(objectives).append("\",\"level\":\"").append(level)
          .append("\",\"skills_targeted\":\"").append(skills).append("\"},");

        sb.append("\"contents\":[");
        for (int i = 0; i < numBlocks; i++) {
            if (i > 0) sb.append(",");
            sb.append("{\"title\":\"Module ").append(i + 1).append("\",\"content_type\":\"text\",\"content_body\":\"Content for module ").append(i + 1).append(". Add your details here.\",\"order_index\":").append(i).append("}");
        }
        sb.append("],\"evaluations\":[");
        String quizContent = "{\\\"questions\\\":[{\\\"text\\\":\\\"Sample question?\\\",\\\"options\\\":[\\\"A\\\",\\\"B\\\",\\\"C\\\"],\\\"correctIndex\\\":0}]}";
        for (int i = 0; i < numBlocks; i++) {
            if (i > 0) sb.append(",");
            sb.append("{\"title\":\"Quiz ").append(i + 1).append("\",\"content\":\"").append(quizContent).append("\",\"passing_score\":75,\"max_attempts\":3}");
        }
        sb.append("],\"exam\":{\"title\":\"Final Exam\",\"duration_minutes\":30,\"passing_score\":75,\"content\":\"").append(quizContent).append("\"}}");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
