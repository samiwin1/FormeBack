package tn.esprit.mentorservice.dto.formation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FormationCatalogItemDto(
        Long id,
        String title,
        String category,
        String level
) {
}
