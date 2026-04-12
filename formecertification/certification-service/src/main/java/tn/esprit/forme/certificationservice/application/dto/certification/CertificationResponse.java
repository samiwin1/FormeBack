package tn.esprit.forme.certificationservice.application.dto.certification;

import tn.esprit.forme.certificationservice.domain.enums.CertificationStatus;

public record CertificationResponse(
        Long id,
        String title,
        String domain,
        String provider,
        String level,
        Integer validityMonths,
        Double thresholdFinal,
        Double weightWritten,
        Double weightOral,
        CertificationStatus status
) {
}
