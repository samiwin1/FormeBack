package tn.esprit.forme.certificationservice.application.mapper;

import org.springframework.stereotype.Component;
import tn.esprit.forme.certificationservice.application.dto.certification.CertificationResponse;
import tn.esprit.forme.certificationservice.application.dto.certification.CreateCertificationRequest;
import tn.esprit.forme.certificationservice.application.dto.certification.UpdateCertificationRequest;
import tn.esprit.forme.certificationservice.domain.entity.CertificationCatalog;
import tn.esprit.forme.certificationservice.domain.enums.CertificationStatus;

@Component

public class CertificationMapper {

    public CertificationCatalog fromCreate(CreateCertificationRequest request) {
        return CertificationCatalog.builder()
                .title(request.title())
                .domain(request.domain())
                .provider(request.provider())
                .level(request.level())
                .validityMonths(request.validityMonths())
                .thresholdFinal(request.thresholdFinal())
                .weightWritten(request.weightWritten())
                .weightOral(request.weightOral())
                .status(CertificationStatus.DRAFT)
                .build();
    }

    public void applyUpdate(CertificationCatalog entity, UpdateCertificationRequest request) {
        entity.setTitle(request.title());
        entity.setDomain(request.domain());
        entity.setProvider(request.provider());
        entity.setLevel(request.level());
        entity.setValidityMonths(request.validityMonths());
        entity.setThresholdFinal(request.thresholdFinal());
        entity.setWeightWritten(request.weightWritten());
        entity.setWeightOral(request.weightOral());
    }

    public CertificationResponse toResponse(CertificationCatalog entity) {
        if (entity == null) return null;
        return new CertificationResponse(
                entity.getId(),
                entity.getTitle() != null ? entity.getTitle() : "",
                entity.getDomain(),
                entity.getProvider(),
                entity.getLevel(),
                entity.getValidityMonths() != null ? entity.getValidityMonths() : 12,
                entity.getThresholdFinal() != null ? entity.getThresholdFinal() : 50.0,
                entity.getWeightWritten() != null ? entity.getWeightWritten() : 0.5,
                entity.getWeightOral() != null ? entity.getWeightOral() : 0.5,
                entity.getStatus() != null ? entity.getStatus() : CertificationStatus.DRAFT
        );
    }
}
