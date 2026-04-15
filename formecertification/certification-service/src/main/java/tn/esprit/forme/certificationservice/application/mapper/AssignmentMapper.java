package tn.esprit.forme.certificationservice.application.mapper;

import org.springframework.stereotype.Component;
import tn.esprit.forme.certificationservice.application.dto.assignment.OralAssignmentResponse;
import tn.esprit.forme.certificationservice.domain.entity.OralExamAssignment;

@Component

public class AssignmentMapper {

    public OralAssignmentResponse toResponse(OralExamAssignment entity) {
        return new OralAssignmentResponse(
                entity.getId(),
                entity.getOralSession().getId(),
                entity.getLearnerId(),
                entity.getStatus(),
                entity.getOralScore(),
                entity.getEvaluatorComment(),
                entity.getGradedAt(),
                entity.getAttemptNumber(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
