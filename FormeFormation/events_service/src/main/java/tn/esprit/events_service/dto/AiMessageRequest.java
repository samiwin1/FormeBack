package tn.esprit.events_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiMessageRequest {

    @NotBlank
    private String message;
}
