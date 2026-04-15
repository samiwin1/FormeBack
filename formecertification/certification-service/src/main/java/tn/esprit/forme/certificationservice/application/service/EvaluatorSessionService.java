package tn.esprit.forme.certificationservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.forme.certificationservice.application.dto.evaluator.EvaluatorAssignmentDto;
import tn.esprit.forme.certificationservice.application.dto.evaluator.EvaluatorSessionDto;
import tn.esprit.forme.certificationservice.application.dto.evaluator.GradeSubmissionRequest;
import tn.esprit.forme.certificationservice.domain.entity.OralExamAssignment;
import tn.esprit.forme.certificationservice.domain.entity.OralSession;
import tn.esprit.forme.certificationservice.domain.enums.AssignmentStatus;
import tn.esprit.forme.certificationservice.domain.enums.OralSessionStatus;
import tn.esprit.forme.certificationservice.domain.repository.OralExamAssignmentRepository;
import tn.esprit.forme.certificationservice.domain.repository.OralSessionRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class EvaluatorSessionService {

    private final OralSessionRepository oralSessionRepository;
    private final OralExamAssignmentRepository assignmentRepository;
    private final UserDirectoryAggregationService userDirectoryAggregationService;

    /**
     * Get all sessions for evaluator — PLANNED and DONE
     */
    public List<EvaluatorSessionDto> getMySessionsWithAssignments(Long evaluatorId) {
        List<OralSessionStatus> statuses = List.of(
                OralSessionStatus.PLANNED,
                OralSessionStatus.DONE
        );
        
        List<OralSession> sessions = oralSessionRepository.findByEvaluatorIdAndStatusIn(evaluatorId, statuses);
        
        return sessions.stream()
                .sorted(Comparator.comparing(OralSession::getScheduledAt).reversed()) // most recent first
                .map(session -> buildEvaluatorSessionDto(session, evaluatorId))
                .toList();
    }

    /**
     * Grade a learner in a session
     */
    public void gradeAssignment(Long evaluatorId, Long assignmentId, GradeSubmissionRequest request) {
        OralExamAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));

        // verify the evaluator owns this session
        OralSession session = assignment.getOralSession();
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found");
        }

        if (!session.getEvaluatorId().equals(evaluatorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not the evaluator for this session");
        }

        // verify assignment is gradable
        if (assignment.getOralScore() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This assignment has already been graded");
        }

        if (assignment.getStatus() != AssignmentStatus.ASSIGNED &&
                assignment.getStatus() != AssignmentStatus.RESCHEDULED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Assignment cannot be graded in status: " + assignment.getStatus());
        }

        assignment.setOralScore(request.oralScore());
        assignment.setEvaluatorComment(request.oralComment());
        assignment.setStatus(AssignmentStatus.COMPLETED);
        assignment.setGradedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        log.info("Evaluator {} graded assignment {} with score {}",
                evaluatorId, assignmentId, request.oralScore());

        // publish event for certificate generation workflow
        // (reuse existing OralScoreSubmittedEvent if it exists)
        // applicationEventPublisher.publishEvent(
        //   new OralScoreSubmittedEvent(assignment));
    }

    /**
     * Mark learner as no-show
     */
    public void markNoShow(Long evaluatorId, Long assignmentId) {
        OralExamAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));

        OralSession session = assignment.getOralSession();
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found");
        }

        if (!session.getEvaluatorId().equals(evaluatorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not the evaluator for this session");
        }

        if (assignment.getStatus() == AssignmentStatus.COMPLETED ||
                assignment.getStatus() == AssignmentStatus.NO_SHOW) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Assignment already finalized");
        }

        assignment.setStatus(AssignmentStatus.NO_SHOW);
        assignmentRepository.save(assignment);

        log.info("Evaluator {} marked assignment {} as NO_SHOW", evaluatorId, assignmentId);
    }

    private EvaluatorSessionDto buildEvaluatorSessionDto(OralSession session, Long evaluatorId) {
        String certTitle = "Unknown Certification";
        if (session.getCertification() != null) {
            certTitle = session.getCertification().getTitle();
        }

        List<OralExamAssignment> assignments = assignmentRepository.findByOralSessionId(session.getId());

        List<EvaluatorAssignmentDto> assignmentDtos = assignments.stream()
                .map(a -> new EvaluatorAssignmentDto(
                        a.getId(),
                        a.getLearnerId(),
                        userDirectoryAggregationService.resolveUserName(a.getLearnerId()),
                        a.getStatus(),
                        a.getOralScore(),
                        a.getEvaluatorComment(),
                        a.getGradedAt(),
                        (a.getStatus() == AssignmentStatus.ASSIGNED ||
                                a.getStatus() == AssignmentStatus.RESCHEDULED)
                                && a.getOralScore() == null  // canGrade
                ))
                .toList();

        return new EvaluatorSessionDto(
                session.getId(),
                session.getTitle(),
                session.getScheduledAt(),
                session.getDurationMinutes(),
                session.getMeetingLink(),
                session.getMeetingProvider().name(),
                certTitle,
                session.getStatus(),
                assignmentDtos
        );
    }
}
