package tn.esprit.forme.certificationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.forme.certificationservice.application.dto.feedback.*;
import tn.esprit.forme.certificationservice.domain.entity.IssuedCertification;
import tn.esprit.forme.certificationservice.domain.entity.OralExamAssignment;
import tn.esprit.forme.certificationservice.domain.entity.OralSession;
import tn.esprit.forme.certificationservice.domain.entity.SessionFeedback;
import tn.esprit.forme.certificationservice.domain.enums.AssignmentStatus;
import tn.esprit.forme.certificationservice.domain.enums.IssuedCertificationStatus;
import tn.esprit.forme.certificationservice.domain.repository.IssuedCertificationRepository;
import tn.esprit.forme.certificationservice.domain.repository.OralExamAssignmentRepository;
import tn.esprit.forme.certificationservice.domain.repository.OralSessionRepository;
import tn.esprit.forme.certificationservice.domain.repository.SessionFeedbackRepository;
import tn.esprit.forme.certificationservice.exception.BusinessException;
import tn.esprit.forme.certificationservice.exception.NotFoundException;

import java.util.List;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor

public class FeedbackService {

    private final SessionFeedbackRepository feedbackRepository;
    private final IssuedCertificationRepository issuedCertificationRepository;
    private final OralSessionRepository oralSessionRepository;
    private final OralExamAssignmentRepository oralExamAssignmentRepository;
    private final NotificationService notificationService;

    @Transactional
    public FeedbackResponse submitFeedback(Long learnerId, SubmitFeedbackRequest request) {
        IssuedCertification issued = issuedCertificationRepository.findById(request.getIssuedCertificationId())
                .orElseThrow(() -> new NotFoundException("Issued certification not found: " + request.getIssuedCertificationId()));

        if (!issued.getLearnerId().equals(learnerId)) {
            throw new AccessDeniedException("Certificate does not belong to current learner");
        }
        if (issued.getStatus() != IssuedCertificationStatus.ISSUED) {
            throw new BusinessException("Certificate not issued");
        }

        if (feedbackRepository.existsByLearnerIdAndIssuedCertificationId(learnerId, issued.getId())) {
            throw new BusinessException("Already submitted");
        }

        SessionFeedback entity = SessionFeedback.builder()
                .learnerId(learnerId)
                .sessionId(request.getSessionId())
                .issuedCertificationId(issued.getId())
                .sessionRating(request.getSessionRating())
                .evaluatorRating(request.getEvaluatorRating())
                .comment(request.getComment())
                .build();

        SessionFeedback saved = feedbackRepository.save(entity);

        return new FeedbackResponse(
                saved.getId(),
                saved.getLearnerId(),
                saved.getSessionId(),
                saved.getIssuedCertificationId(),
                saved.getSessionRating(),
                saved.getEvaluatorRating(),
                saved.getComment(),
                saved.getSubmittedAt()
        );
    }

    @Transactional(readOnly = true)
    public PendingFeedbackDto hasPendingFeedback(Long learnerId) {
        List<IssuedCertification> issuedCertificates =
                issuedCertificationRepository.findByLearnerIdAndStatus(learnerId, IssuedCertificationStatus.ISSUED);

        if (issuedCertificates.isEmpty()) {
            return new PendingFeedbackDto(false, null, null);
        }

        List<Long> withFeedback =
                feedbackRepository.findIssuedCertificationIdsWithFeedbackByLearnerId(learnerId);

        for (IssuedCertification cert : issuedCertificates) {
            if (withFeedback.contains(cert.getId())) {
                continue;
            }

            Long sessionId = resolveSessionIdForCertificate(learnerId, cert);
            return new PendingFeedbackDto(true, cert.getId(), sessionId);
        }

        return new PendingFeedbackDto(false, null, null);
    }

    private Long resolveSessionIdForCertificate(Long learnerId, IssuedCertification cert) {
        // Best-effort: find latest COMPLETED assignment for this learner + certification
        return oralExamAssignmentRepository
                .findByLearnerId(learnerId).stream()
                .filter(a -> a.getOralSession() != null
                        && a.getOralSession().getCertification() != null
                        && cert.getCertification().getId().equals(a.getOralSession().getCertification().getId()))
                .filter(a -> a.getStatus() == AssignmentStatus.COMPLETED)
                .sorted((a, b) -> {
                    if (a.getGradedAt() == null && b.getGradedAt() == null) return 0;
                    if (a.getGradedAt() == null) return 1;
                    if (b.getGradedAt() == null) return -1;
                    return b.getGradedAt().compareTo(a.getGradedAt());
                })
                .map(OralExamAssignment::getOralSession)
                .map(OralSession::getId)
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbackForSession(Long sessionId) {
        return feedbackRepository.findBySessionId(sessionId).stream()
                .map(f -> new FeedbackResponse(
                        f.getId(),
                        f.getLearnerId(),
                        f.getSessionId(),
                        f.getIssuedCertificationId(),
                        f.getSessionRating(),
                        f.getEvaluatorRating(),
                        f.getComment(),
                        f.getSubmittedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionFeedbackSummaryDto getSessionFeedbackSummary(Long sessionId) {
        double avgSession = feedbackRepository.findAvgSessionRatingBySessionId(sessionId).orElse(0.0d);

        OralSession session = oralSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found: " + sessionId));

        double avgEvaluator = feedbackRepository.findAvgEvaluatorRatingByEvaluatorId(session.getEvaluatorId())
                .orElse(0.0d);

        int total = feedbackRepository.findBySessionId(sessionId).size();

        return new SessionFeedbackSummaryDto(
                sessionId,
                session.getTitle(),
                avgSession,
                avgEvaluator,
                total
        );
    }

    @Transactional(readOnly = true)
    public OptionalDouble getAvgEvaluatorRating(Long evaluatorId) {
        return feedbackRepository.findAvgEvaluatorRatingByEvaluatorId(evaluatorId)
                .map(value -> OptionalDouble.of(value))
                .orElseGet(OptionalDouble::empty);
    }
}

