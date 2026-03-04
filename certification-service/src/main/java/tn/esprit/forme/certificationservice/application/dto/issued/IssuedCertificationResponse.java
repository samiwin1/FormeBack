package tn.esprit.forme.certificationservice.application.dto.issued;

import tn.esprit.forme.certificationservice.domain.enums.IssuedCertificationStatus;

import java.time.LocalDateTime;

public record IssuedCertificationResponse(
        Long id,
        Long learnerId,
        Long certificationId,
        Long formationId,
        Double writtenScore,
        Double oralScore,
        Double finalScore,
        LocalDateTime issuedAt,
        LocalDateTime expiresAt,
        String certificateNumber,
        IssuedCertificationStatus status,
        String pdfPath
) {
}
