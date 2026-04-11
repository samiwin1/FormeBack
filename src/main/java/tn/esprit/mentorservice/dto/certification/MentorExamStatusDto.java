package tn.esprit.mentorservice.dto.certification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MentorExamStatusDto(
        Long formationId,
        Double writtenScore,
        Boolean writtenPassed,
        String oralStatus,
        LocalDateTime oralScheduledAt,
        String meetingLink,
        Double oralScore
) {
}
