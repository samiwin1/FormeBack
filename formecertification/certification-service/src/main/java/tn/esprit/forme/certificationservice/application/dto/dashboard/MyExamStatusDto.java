package tn.esprit.forme.certificationservice.application.dto.dashboard;

import java.time.LocalDateTime;

public record MyExamStatusDto(
        Long formationId,
        Double writtenScore,
        Boolean writtenPassed,
        String oralStatus,
        LocalDateTime oralScheduledAt,
        String meetingLink,
        Double oralScore
) {
}
