package tn.esprit.mentorservice.dto.certification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MentorLearnerContextDto(
        MentorExamStatusDto examStatus,
        MentorCertificationStatusDto certificationStatus,
        List<CertificationCatalogItemDto> publishedCertifications
) {
}
