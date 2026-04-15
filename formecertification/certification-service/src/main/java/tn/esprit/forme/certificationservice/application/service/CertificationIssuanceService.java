package tn.esprit.forme.certificationservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.forme.certificationservice.domain.entity.CertificationCatalog;
import tn.esprit.forme.certificationservice.domain.entity.IssuedCertification;
import tn.esprit.forme.certificationservice.domain.enums.AssignmentStatus;
import tn.esprit.forme.certificationservice.domain.enums.IssuedCertificationStatus;
import tn.esprit.forme.certificationservice.domain.repository.OralExamAssignmentRepository;
import tn.esprit.forme.certificationservice.domain.repository.IssuedCertificationRepository;
import tn.esprit.forme.certificationservice.exception.BusinessException;
import tn.esprit.forme.certificationservice.exception.NotFoundException;
import tn.esprit.forme.certificationservice.infrastructure.feign.FormationClient;
import tn.esprit.forme.certificationservice.infrastructure.feign.UserClient;
import tn.esprit.forme.certificationservice.infrastructure.feign.dto.FormationDto;
import tn.esprit.forme.certificationservice.application.dto.issued.IssuedCertificationResponse;
import tn.esprit.forme.certificationservice.application.mapper.IssuedCertificationMapper;
import tn.esprit.forme.certificationservice.security.SecurityUtils;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j

public class CertificationIssuanceService {

    private final ScoringService scoringService;
    private final PdfGenerationService pdfGenerationService;
    private final IssuedCertificationRepository issuedCertificationRepository;
    private final OralExamAssignmentRepository oralAssignmentRepository;
    private final EligibilityService eligibilityService;
    private final UserClient userClient;
    private final FormationClient formationClient;
    private final EmailNotificationService emailNotificationService;
    private final NotificationService notificationService;
    private final CertificateEventsService certificateEventsService;
    private final IssuedCertificationMapper issuedCertificationMapper;

    @Transactional
    public Optional<IssuedCertification> issueIfEligible(Long learnerId,
                                                         Long formationId,
                                                         CertificationCatalog catalog,
                                                         double writtenScore,
                                                         double oralScore) {
        double finalScore = scoringService.computeFinalScore(
                writtenScore,
                oralScore,
                catalog.getWeightWritten(),
                catalog.getWeightOral()
        );

        if (finalScore < catalog.getThresholdFinal()) {
            return Optional.empty();
        }

        if (issuedCertificationRepository.existsByLearnerIdAndCertificationIdAndFormationId(
                learnerId, catalog.getId(), formationId)) {
            return issuedCertificationRepository.findByLearnerIdAndCertificationIdAndFormationId(
                    learnerId, catalog.getId(), formationId);
        }

        FormationDto formation = formationClient.getFormationById(formationId);
        String learnerName = resolveLearnerName(learnerId);

        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = issuedAt.plusMonths(catalog.getValidityMonths());
        String certificateNumber = nextCertificateNumber(issuedAt.getYear());
        String pdfPath = pdfGenerationService.generateCertificatePdf(
                learnerName,
                formation.getTitle(),
                catalog.getTitle(),
                finalScore,
                issuedAt,
                certificateNumber
        );

        IssuedCertification issued = IssuedCertification.builder()
                .learnerId(learnerId)
                .certification(catalog)
                .formationId(formationId)
                .writtenScore(writtenScore)
                .oralScore(oralScore)
                .finalScore(finalScore)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .certificateNumber(certificateNumber)
                .status(IssuedCertificationStatus.ISSUED)
                .pdfPath(pdfPath)
                .build();

        IssuedCertification saved = issuedCertificationRepository.save(issued);
        emailNotificationService.sendCertificateIssued(learnerId, catalog.getTitle(), finalScore);
        notificationService.notifyCertificateIssued(
                learnerId,
                catalog.getTitle(),
                finalScore,
                saved.getId()
        );
        notificationService.notifyFeedbackRequested(
                learnerId,
                catalog.getTitle(),
                null,
                saved.getId()
        );
        certificateEventsService.publishCertificateReady(
                learnerId,
                saved.getId(),
                saved.getCertificateNumber()
        );
        return Optional.of(saved);
    }

    private String resolveLearnerName(Long learnerId) {
        try {
            var user = userClient.getUserById(learnerId);
            if (user != null) {
                String fullName = user.getFullName();
                if (fullName != null && !fullName.isBlank()) {
                    return fullName;
                }
            }
        } catch (Exception ex) {
            // Fall back when user-service is unavailable
        }
        return "Learner #" + learnerId;
    }

    private String nextCertificateNumber(int year) {
        String prefix = "FORME-" + year + "-";
        IssuedCertification last = issuedCertificationRepository
                .findTopByCertificateNumberStartingWithOrderByCertificateNumberDescWithLock(prefix);

        int next = 1;
        if (last != null && last.getCertificateNumber() != null) {
            String[] parts = last.getCertificateNumber().split("-");
            if (parts.length == 3) {
                try {
                    next = Integer.parseInt(parts[2]) + 1;
                } catch (NumberFormatException ex) {
                    log.warn("Invalid certificate number format: {}", last.getCertificateNumber());
                    next = 1;
                }
            }
        }

        return String.format("%s%06d", prefix, next);
    }

    @Transactional
    public IssuedCertificationResponse manualIssue(Long assignmentId) {
        var assignment = oralAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found: " + assignmentId));
        if (assignment.getStatus() != AssignmentStatus.COMPLETED) {
            throw new BusinessException("Assignment must be completed before issuing certificate");
        }
        if (assignment.getFormationId() == null) {
            throw new BusinessException("Assignment formation is missing");
        }
        if (assignment.getOralScore() == null) {
            throw new BusinessException("Oral score is missing");
        }

        var written = eligibilityService.requireWrittenExamPassed(assignment.getLearnerId(), assignment.getFormationId());
        var issued = issueIfEligible(
                assignment.getLearnerId(),
                assignment.getFormationId(),
                assignment.getOralSession().getCertification(),
                written.getScore(),
                assignment.getOralScore()
        ).orElseThrow(() -> new BusinessException("Learner final score is below certification threshold"));

        notificationService.notifyFeedbackRequested(
                assignment.getLearnerId(),
                assignment.getOralSession().getCertification().getTitle(),
                assignment.getOralSession().getId(),
                issued.getId()
        );

        return issuedCertificationMapper.toResponse(issued);
    }
}
