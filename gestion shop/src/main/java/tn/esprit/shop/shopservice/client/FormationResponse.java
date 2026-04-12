package tn.esprit.shop.shopservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for formation data returned by the external formation-service.
 * Used when calling GET /api/formations/{id}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormationResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private String level;
}
