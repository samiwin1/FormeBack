package tn.esprit.forme.certificationservice.application.dto.evaluator;

import tn.esprit.forme.certificationservice.domain.enums.OralSessionStatus;

import java.time.LocalDateTime;
import java.util.List;

public record EvaluatorSessionDto(
        Long sessionId,
        String title,
        LocalDateTime scheduledAt,
        int durationMinutes,
        String meetingLink,
        String meetingProvider,
        String certificationTitle,
        OralSessionStatus status,
        List<EvaluatorAssignmentDto> assignments
) {}
