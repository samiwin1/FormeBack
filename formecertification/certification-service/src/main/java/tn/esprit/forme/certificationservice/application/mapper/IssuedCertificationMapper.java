package tn.esprit.forme.certificationservice.application.mapper;

import org.springframework.stereotype.Component;
import tn.esprit.forme.certificationservice.application.dto.issued.IssuedCertificationResponse;
import tn.esprit.forme.certificationservice.domain.entity.IssuedCertification;

@Component

public class IssuedCertificationMapper {

    public IssuedCertificationResponse toResponse(IssuedCertification entity) {
        return new IssuedCertificationResponse(
                entity.getId(),
                entity.getLearnerId(),
                entity.getCertification().getId(),
                entity.getFormationId(),
                entity.getWrittenScore(),
                entity.getOralScore(),
                entity.getFinalScore(),
                entity.getIssuedAt(),
                entity.getExpiresAt(),
                entity.getCertificateNumber(),
                entity.getStatus(),
                entity.getPdfPath()
        );
    }
}
