package tn.esprit.forme.certificationservice.infrastructure.feign.dto;

import lombok.Data;

@Data

public class WrittenExamResultDto {
    private Long learnerId;
    private Long formationId;
    private Double score;
    private Boolean passed;
}
