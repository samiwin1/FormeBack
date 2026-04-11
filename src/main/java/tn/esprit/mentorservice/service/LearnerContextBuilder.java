package tn.esprit.mentorservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tn.esprit.mentorservice.dto.LearningPathContext;
import tn.esprit.mentorservice.dto.certification.MentorLearnerContextDto;
import tn.esprit.mentorservice.dto.formation.FormationCatalogItemDto;
import tn.esprit.mentorservice.dto.formation.LearnerProgressSnapshotDto;
import tn.esprit.mentorservice.entity.LearnerPortfolio;
import tn.esprit.mentorservice.entity.LearnerSkill;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LearnerContextBuilder {

    private final ObjectMapper objectMapper;

    public String buildPortfolioJson(LearnerPortfolio p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userId", p.getUserId());
        m.put("currentRole", p.getCurrentRole());
        m.put("targetRole", p.getTargetRole());
        m.put("experienceLevel", p.getExperienceLevel().name());
        m.put("bio", p.getBio());
        m.put("weeklyStudyHours", p.getWeeklyStudyHours());
        m.put("preferredLanguage", p.getPreferredLanguage());
        m.put("learningStyle", p.getLearningStyle().name());
        if (p.getExtendedPreferences() != null) {
            m.put("extendedPreferences", p.getExtendedPreferences());
        }
        List<Map<String, String>> skills = p.getSkills().stream()
                .map(this::skillMap)
                .collect(Collectors.toList());
        m.put("selfAssessedSkills", skills);
        try {
            return objectMapper.writeValueAsString(m);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Map<String, String> skillMap(LearnerSkill s) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("name", s.getSkillName());
        m.put("level", s.getSkillLevel().name());
        return m;
    }

    public String buildProgressJson(LearnerProgressSnapshotDto snapshot) {
        if (snapshot == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    public String buildFormationCatalogJson(List<FormationCatalogItemDto> items) {
        if (items == null || items.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    public String buildCertificationContextJson(MentorLearnerContextDto ctx) {
        if (ctx == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(ctx);
        } catch (JsonProcessingException e) {
            return "null";
        }
    }

    /**
     * Aggregates all data sources into a {@link LearningPathContext} ready for prompt construction.
     * Empty catalog / absent progress / absent cert context are noted in {@code unavailableSources}
     * so the prompt can tell the AI which data was missing.
     */
    public LearningPathContext buildLearningPathContext(
            LearnerPortfolio portfolio,
            List<FormationCatalogItemDto> catalog,
            Optional<LearnerProgressSnapshotDto> progress,
            Optional<MentorLearnerContextDto> certContext
    ) {
        List<String> unavailable = new ArrayList<>();
        if (catalog == null || catalog.isEmpty()) {
            unavailable.add("formation-catalog");
        }
        if (progress == null || progress.isEmpty()) {
            unavailable.add("formation-progress");
        }
        if (certContext == null || certContext.isEmpty()) {
            unavailable.add("certification-context");
        }
        Optional<LearnerProgressSnapshotDto> safeProgress = progress != null ? progress : Optional.empty();
        Optional<MentorLearnerContextDto> safeCert = certContext != null ? certContext : Optional.empty();
        return new LearningPathContext(
                buildPortfolioJson(portfolio),
                buildFormationCatalogJson(catalog),
                safeProgress.map(this::buildProgressJson).orElse("null"),
                safeCert.map(this::buildCertificationContextJson).orElse("null"),
                unavailable
        );
    }

}
