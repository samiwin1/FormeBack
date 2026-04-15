package tn.esprit.forme.certificationservice.application.mapper;

import org.springframework.stereotype.Component;
import tn.esprit.forme.certificationservice.application.dto.oralsession.CreateOralSessionRequest;
import tn.esprit.forme.certificationservice.application.dto.oralsession.OralSessionResponse;
import tn.esprit.forme.certificationservice.application.dto.oralsession.UpdateOralSessionRequest;
import tn.esprit.forme.certificationservice.domain.entity.CertificationCatalog;
import tn.esprit.forme.certificationservice.domain.entity.OralSession;
import tn.esprit.forme.certificationservice.domain.enums.OralSessionStatus;

@Component

public class OralSessionMapper {

    public OralSession fromCreate(CreateOralSessionRequest request, CertificationCatalog certification) {
        return OralSession.builder()
                .certification(certification)
                .title(request.title())
                .scheduledAt(request.scheduledAt())
                .durationMinutes(request.durationMinutes())
                .meetingProvider(request.meetingProvider())
                .meetingLink(request.meetingLink())
                .evaluatorId(request.evaluatorId())
                .status(OralSessionStatus.PLANNED)
                .build();
    }

    public void applyUpdate(OralSession entity, UpdateOralSessionRequest request) {
        entity.setTitle(request.title());
        entity.setScheduledAt(request.scheduledAt());
        entity.setDurationMinutes(request.durationMinutes());
        entity.setMeetingProvider(request.meetingProvider());
        entity.setMeetingLink(request.meetingLink());
        entity.setEvaluatorId(request.evaluatorId());
        entity.setStatus(request.status());
    }

    public OralSessionResponse toResponse(OralSession entity) {
        return new OralSessionResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getCertification().getId(),
                entity.getCertification() == null ? null : entity.getCertification().getTitle(),
                entity.getScheduledAt(),
                entity.getDurationMinutes(),
                entity.getMeetingProvider(),
                entity.getMeetingLink(),
                entity.getEvaluatorId(),
                null,
                entity.getStatus(),
                null
        );
    }
}
