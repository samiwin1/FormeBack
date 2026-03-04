package tn.esprit.forme.certificationservice.application.dto.oralsession;

import tn.esprit.forme.certificationservice.domain.enums.MeetingProvider;
import tn.esprit.forme.certificationservice.domain.enums.OralSessionStatus;

import java.time.LocalDateTime;

public record OralSessionResponse(
        Long id,
        String title,
        Long certificationId,
        String certificationTitle,
        LocalDateTime scheduledAt,
        Integer durationMinutes,
        MeetingProvider meetingProvider,
        String meetingLink,
        Long evaluatorId,
        String evaluatorName,
        OralSessionStatus status,
        Integer learnerCount
) {
}
