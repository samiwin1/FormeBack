package tn.esprit.forme.certificationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.forme.certificationservice.application.dto.reschedule.CreateRescheduleRequest;
import tn.esprit.forme.certificationservice.application.dto.reschedule.RescheduleResponse;
import tn.esprit.forme.certificationservice.application.mapper.RescheduleMapper;
import tn.esprit.forme.certificationservice.domain.entity.RescheduleRequest;
import tn.esprit.forme.certificationservice.domain.enums.OralSessionStatus;
import tn.esprit.forme.certificationservice.domain.enums.RescheduleStatus;
import tn.esprit.forme.certificationservice.exception.BusinessException;
import tn.esprit.forme.certificationservice.domain.repository.RescheduleRequestRepository;
import tn.esprit.forme.certificationservice.exception.NotFoundException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor

public class RescheduleService {

    private final RescheduleRequestRepository repository;
    private final OralAssignmentService oralAssignmentService;
    private final OralSessionService oralSessionService;
    private final EmailNotificationService emailNotificationService;
    private final NotificationService notificationService;
    private final RescheduleMapper mapper;

    @Transactional
    public RescheduleResponse create(Long assignmentId, CreateRescheduleRequest request, Long learnerId) {
        var assignment = oralAssignmentService.findEntity(assignmentId);
        if (!assignment.getLearnerId().equals(learnerId)) {
            throw new IllegalArgumentException("Learner can only request reschedule for own assignment");
        }

        oralAssignmentService.markRescheduleRequested(assignmentId);

        RescheduleRequest entity = RescheduleRequest.builder()
                .assignment(assignment)
                .proposedDatetime(request.proposedDatetime())
                .message(request.message())
                .requestedAt(LocalDateTime.now())
                .status(RescheduleStatus.PENDING)
                .build();

        return mapper.toResponse(repository.save(entity));
    }

    @Transactional
    public RescheduleResponse approve(Long id, String adminComment) {
        RescheduleRequest request = findEntity(id);
        if (request.getStatus() != RescheduleStatus.PENDING) {
            throw new BusinessException("Only pending requests can be approved");
        }

        request.setStatus(RescheduleStatus.APPROVED);
        request.setDecidedAt(LocalDateTime.now());
        request.setAdminComment(adminComment);
        repository.save(request);

        var assignment = request.getAssignment();
        // Set assignment-specific schedule instead of modifying the session
        assignment.setScheduledAtOverride(request.getProposedDatetime());
        oralAssignmentService.markRescheduled(assignment.getId());

        return mapper.toResponse(request);
    }

    @Transactional
    public RescheduleResponse reject(Long id, String adminComment, Long replacementSessionId) {
        RescheduleRequest request = findEntity(id);
        if (request.getStatus() != RescheduleStatus.PENDING) {
            throw new BusinessException("Only pending requests can be rejected");
        }

        var assignment = request.getAssignment();
        if (replacementSessionId != null) {
            var replacement = oralSessionService.findEntity(replacementSessionId);
            var currentCertificationId = assignment.getOralSession().getCertification().getId();
            var replacementCertificationId = replacement.getCertification().getId();
            if (!currentCertificationId.equals(replacementCertificationId)) {
                throw new BusinessException("Replacement session must belong to the same certification");
            }
            if (replacement.getStatus() != OralSessionStatus.PLANNED) {
                throw new BusinessException("Replacement session must be in PLANNED status");
            }
            if (replacement.getScheduledAt() != null && replacement.getScheduledAt().isBefore(LocalDateTime.now())) {
                throw new BusinessException("Replacement session must be scheduled in the future");
            }

            assignment.setOralSession(replacement);
            oralAssignmentService.markRescheduled(assignment.getId());
            emailNotificationService.sendSessionAssigned(
                    assignment.getLearnerId(),
                    replacement.getTitle(),
                    replacement.getScheduledAt(),
                    replacement.getMeetingLink()
            );
            notificationService.notifySessionAssigned(
                    assignment.getLearnerId(),
                    replacement.getTitle(),
                    replacement.getScheduledAt(),
                    replacement.getMeetingLink(),
                    replacement.getId()
            );
        }

        request.setStatus(RescheduleStatus.REJECTED);
        request.setDecidedAt(LocalDateTime.now());
        request.setAdminComment(adminComment);
        return mapper.toResponse(repository.save(request));
    }

    private RescheduleRequest findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reschedule request not found: " + id));
    }
}
