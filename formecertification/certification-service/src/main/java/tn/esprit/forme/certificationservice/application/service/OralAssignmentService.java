package tn.esprit.forme.certificationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.forme.certificationservice.application.dto.assignment.*;
import tn.esprit.forme.certificationservice.application.mapper.AssignmentMapper;
import tn.esprit.forme.certificationservice.domain.entity.OralExamAssignment;
import tn.esprit.forme.certificationservice.domain.enums.AssignmentStatus;
import tn.esprit.forme.certificationservice.domain.repository.OralExamAssignmentRepository;
import tn.esprit.forme.certificationservice.exception.BusinessException;
import tn.esprit.forme.certificationservice.exception.NotFoundException;
import tn.esprit.forme.certificationservice.infrastructure.feign.dto.WrittenExamResultDto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor

public class OralAssignmentService {

    private final OralExamAssignmentRepository repository;
    private final OralSessionService oralSessionService;
    private final EligibilityService eligibilityService;
    private final CertificationIssuanceService issuanceService;
    private final EmailNotificationService emailNotificationService;
    private final NotificationService notificationService;
    private final ScoringService scoringService;
    private final AssignmentMapper mapper;

    @Transactional
    public OralAssignmentResponse assign(CreateAssignmentRequest request) {
        var oralSession = oralSessionService.findEntity(request.oralSessionId());
        eligibilityService.requireWrittenExamPassed(request.learnerId(), request.formationId());
        Long certificationId = oralSession.getCertification().getId();

        boolean hasActiveAssignment = repository.existsByLearnerIdAndOralSessionCertificationIdAndStatusIn(
                request.learnerId(),
                certificationId,
                Arrays.asList(
                        AssignmentStatus.ASSIGNED,
                        AssignmentStatus.RESCHEDULE_REQUESTED,
                        AssignmentStatus.RESCHEDULED
                )
        );
        if (hasActiveAssignment) {
            throw new BusinessException("Learner already has an active assignment for this certification");
        }

        int nextAttempt = repository
                .findTopByLearnerIdAndOralSessionCertificationIdOrderByAttemptNumberDescCreatedAtDesc(
                        request.learnerId(),
                        certificationId
                )
                .map(item -> item.getAttemptNumber() == null ? 1 : item.getAttemptNumber() + 1)
                .orElse(1);

        if (nextAttempt > 2) {
            throw new BusinessException("Maximum 2 oral attempts reached for this learner and certification");
        }

        OralExamAssignment assignment = OralExamAssignment.builder()
                .oralSession(oralSession)
                .learnerId(request.learnerId())
                .formationId(request.formationId())
                .status(AssignmentStatus.ASSIGNED)
                .attemptNumber(nextAttempt)
                .build();

        OralExamAssignment saved = repository.save(assignment);
        emailNotificationService.sendSessionAssigned(
                saved.getLearnerId(),
                oralSession.getTitle(),
                oralSession.getScheduledAt(),
                oralSession.getMeetingLink()
        );
        notificationService.notifySessionAssigned(
                saved.getLearnerId(),
                oralSession.getTitle(),
                oralSession.getScheduledAt(),
                oralSession.getMeetingLink(),
                oralSession.getId()
        );

        return mapper.toResponse(saved);
    }

    @Transactional
    public OralAssignmentResponse grade(Long assignmentId, GradeAssignmentRequest request, Long evaluatorId) {
        OralExamAssignment assignment = findEntity(assignmentId);

        var session = assignment.getOralSession();
        if (!session.getEvaluatorId().equals(evaluatorId)) {
            throw new BusinessException("Evaluator can only grade assigned sessions");
        }

        if (assignment.getStatus() == AssignmentStatus.COMPLETED || assignment.getStatus() == AssignmentStatus.FAILED) {
            throw new BusinessException("Assignment already graded");
        }

        // Use assignment's formationId, not request's (security fix)
        Long formationId = assignment.getFormationId();
        if (formationId == null) {
            throw new BusinessException("Assignment must have a formationId before grading");
        }

        WrittenExamResultDto writtenResult = eligibilityService.requireWrittenExamPassed(
                assignment.getLearnerId(), formationId);
        var catalog = session.getCertification();
        double finalScore = scoringService.computeFinalScore(
                writtenResult.getScore(),
                request.oralScore(),
                catalog.getWeightWritten(),
                catalog.getWeightOral()
        );
        boolean passed = finalScore >= catalog.getThresholdFinal();
        int attempt = assignment.getAttemptNumber() == null ? 1 : assignment.getAttemptNumber();

        assignment.setOralScore(request.oralScore());
        assignment.setEvaluatorComment(request.evaluatorComment());
        assignment.setGradedAt(LocalDateTime.now());
        assignment.setStatus(!passed && attempt >= 2 ? AssignmentStatus.FAILED : AssignmentStatus.COMPLETED);
        repository.save(assignment);

        if (passed) {
            issuanceService.issueIfEligible(
                    assignment.getLearnerId(),
                    formationId,
                    catalog,
                    writtenResult.getScore(),
                    request.oralScore()
            );
        }

        return mapper.toResponse(assignment);
    }

    @Transactional(readOnly = true)
    public List<OralAssignmentResponse> findByLearner(Long learnerId) {
        return repository.findByLearnerId(learnerId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OralAssignmentResponse> findByEvaluator(Long evaluatorId) {
        return repository.findByOralSessionEvaluatorId(evaluatorId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OralExamAssignment findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Assignment not found: " + id));
    }

    @Transactional
    public void markRescheduleRequested(Long assignmentId) {
        OralExamAssignment assignment = findEntity(assignmentId);
        assignment.setStatus(AssignmentStatus.RESCHEDULE_REQUESTED);
        repository.save(assignment);
    }

    @Transactional
    public void markRescheduled(Long assignmentId) {
        OralExamAssignment assignment = findEntity(assignmentId);
        assignment.setStatus(AssignmentStatus.RESCHEDULED);
        repository.save(assignment);
    }

    @Transactional
    public void markNoShow(Long assignmentId, Long evaluatorId) {
        OralExamAssignment assignment = findEntity(assignmentId);
        if (!assignment.getOralSession().getEvaluatorId().equals(evaluatorId)) {
            throw new BusinessException("Evaluator can only update assigned sessions");
        }
        assignment.setStatus(AssignmentStatus.NO_SHOW);
        repository.save(assignment);
    }
}
