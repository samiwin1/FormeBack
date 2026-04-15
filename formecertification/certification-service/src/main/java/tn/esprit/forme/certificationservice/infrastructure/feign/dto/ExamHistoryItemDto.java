package tn.esprit.forme.certificationservice.infrastructure.feign.dto;

import lombok.Data;

import java.time.Instant;

@Data

public class ExamHistoryItemDto {
    private Long examenId;
    private String examTitle;
    private Long formationId;
    private Integer score;
    private Boolean passed;
    private Long durationMinutes;
    private Instant submittedAt;
}
