package tn.esprit.forme.certificationservice.application.dto.dashboard;

public record EligibleLearnerDto(
        Long learnerId,
        String learnerName,
        Long formationId,
        String formationTitle,
        Double writtenScore,
        Boolean passed
) {
}
