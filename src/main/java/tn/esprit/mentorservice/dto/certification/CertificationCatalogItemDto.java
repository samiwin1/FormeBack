package tn.esprit.mentorservice.dto.certification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CertificationCatalogItemDto(
        Long id,
        String title,
        String domain,
        String provider,
        String level
) {
}
