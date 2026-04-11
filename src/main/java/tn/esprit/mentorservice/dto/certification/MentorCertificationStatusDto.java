package tn.esprit.mentorservice.dto.certification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MentorCertificationStatusDto(
        String status,
        Long issuedCertificationId,
        String certificateNumber,
        Double finalScore,
        String pdfPath,
        Boolean downloadable
) {
}
