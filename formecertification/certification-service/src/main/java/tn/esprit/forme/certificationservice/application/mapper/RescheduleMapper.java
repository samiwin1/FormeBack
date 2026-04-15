package tn.esprit.forme.certificationservice.application.mapper;

import org.springframework.stereotype.Component;
import tn.esprit.forme.certificationservice.application.dto.reschedule.RescheduleResponse;
import tn.esprit.forme.certificationservice.domain.entity.RescheduleRequest;

@Component

public class RescheduleMapper {

    public RescheduleResponse toResponse(RescheduleRequest entity) {
        return new RescheduleResponse(
                entity.getId(),
                entity.getAssignment().getId(),
                entity.getProposedDatetime(),
                entity.getMessage(),
                entity.getRequestedAt(),
                entity.getDecidedAt(),
                entity.getAdminComment(),
                entity.getStatus()
        );
    }
}
