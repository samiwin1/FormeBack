package tn.esprit.mentorservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.mentorservice.domain.StudyPlanItemType;
import tn.esprit.mentorservice.entity.MentorSession;
import tn.esprit.mentorservice.entity.StudyPlan;
import tn.esprit.mentorservice.entity.StudyPlanItem;
import tn.esprit.mentorservice.repository.StudyPlanRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyPlanPersistenceService {

    private final StudyPlanRepository studyPlanRepository;

    @Transactional
    public void tryPersistFromAdviceJson(long userId, MentorSession session, JsonNode root) {
        if (root == null) {
            return;
        }
        JsonNode plan = root.get("studyPlan");
        if (plan == null || plan.isNull() || !plan.isObject()) {
            return;
        }
        String title = plan.path("title").asText("Study plan").trim();
        if (title.isEmpty()) {
            title = "Study plan";
        }
        JsonNode items = plan.get("items");
        if (items == null || !items.isArray() || items.isEmpty()) {
            return;
        }

        StudyPlan sp = StudyPlan.builder()
                .userId(userId)
                .sourceSession(session)
                .title(title)
                .build();

        List<StudyPlanItem> built = new ArrayList<>();
        int order = 0;
        for (JsonNode it : items) {
            if (!it.isObject()) {
                continue;
            }
            StudyPlanItemType type = parseType(it.path("type").asText("READ"));
            String itemTitle = it.path("title").asText("").trim();
            if (itemTitle.isEmpty()) {
                continue;
            }
            int dur = it.path("durationMinutes").asInt(0);
            Integer duration = dur > 0 ? dur : null;
            String ref = it.path("resourceRef").asText(null);
            if (ref != null && ref.isBlank()) {
                ref = null;
            }
            StudyPlanItem row = StudyPlanItem.builder()
                    .studyPlan(sp)
                    .sortOrder(order++)
                    .itemType(type)
                    .title(itemTitle)
                    .durationMinutes(duration)
                    .resourceRef(ref)
                    .build();
            built.add(row);
        }
        if (built.isEmpty()) {
            return;
        }
        sp.getItems().addAll(built);
        studyPlanRepository.save(sp);
    }

    private static StudyPlanItemType parseType(String raw) {
        if (raw == null) {
            return StudyPlanItemType.READ;
        }
        try {
            return StudyPlanItemType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return StudyPlanItemType.READ;
        }
    }
}
