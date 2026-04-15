package tn.esprit.forme.certificationservice.infrastructure.feign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data

public class ResultExamenDto {
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    private Integer score;
    private Boolean passed;
}
