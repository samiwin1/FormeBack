package tn.esprit.forme.certificationservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tn.esprit.forme.certificationservice.domain.entity.CertificationCatalog;
import tn.esprit.forme.certificationservice.domain.entity.IssuedCertification;
import tn.esprit.forme.certificationservice.domain.entity.OralExamAssignment;
import tn.esprit.forme.certificationservice.domain.entity.OralSession;
import tn.esprit.forme.certificationservice.domain.entity.RescheduleRequest;
import tn.esprit.forme.certificationservice.domain.enums.AssignmentStatus;
import tn.esprit.forme.certificationservice.domain.enums.CertificationStatus;
import tn.esprit.forme.certificationservice.domain.enums.IssuedCertificationStatus;
import tn.esprit.forme.certificationservice.domain.enums.MeetingProvider;
import tn.esprit.forme.certificationservice.domain.enums.OralSessionStatus;
import tn.esprit.forme.certificationservice.domain.enums.RescheduleStatus;
import tn.esprit.forme.certificationservice.domain.repository.CertificationCatalogRepository;
import tn.esprit.forme.certificationservice.domain.repository.IssuedCertificationRepository;
import tn.esprit.forme.certificationservice.domain.repository.OralExamAssignmentRepository;
import tn.esprit.forme.certificationservice.domain.repository.OralSessionRepository;
import tn.esprit.forme.certificationservice.domain.repository.RescheduleRequestRepository;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DemoDataSeeder {

    @Bean
    @ConditionalOnProperty(name = "app.seed.demo-data", havingValue = "true", matchIfMissing = true)
    CommandLineRunner seedCertificationDemoData(
            CertificationCatalogRepository certificationCatalogRepository,
            OralSessionRepository oralSessionRepository,
            OralExamAssignmentRepository assignmentRepository,
            RescheduleRequestRepository rescheduleRequestRepository,
            IssuedCertificationRepository issuedCertificationRepository
    ) {
        return args -> {
            boolean hasAnyData = certificationCatalogRepository.count() > 0
                    || oralSessionRepository.count() > 0
                    || assignmentRepository.count() > 0
                    || rescheduleRequestRepository.count() > 0
                    || issuedCertificationRepository.count() > 0;

            if (hasAnyData) {
                return;
            }

            CertificationCatalog javaCatalog = certificationCatalogRepository.save(
                    CertificationCatalog.builder()
                            .title("Java Backend Professional")
                            .domain("Backend")
                            .provider("ForME")
                            .level("Intermediate")
                            .validityMonths(24)
                            .thresholdFinal(70.0)
                            .weightWritten(0.0)
                            .weightOral(1.0)
                            .status(CertificationStatus.PUBLISHED)
                            .build()
            );

            CertificationCatalog angularCatalog = certificationCatalogRepository.save(
                    CertificationCatalog.builder()
                            .title("Angular Frontend Specialist")
                            .domain("Frontend")
                            .provider("ForME")
                            .level("Advanced")
                            .validityMonths(24)
                            .thresholdFinal(70.0)
                            .weightWritten(0.0)
                            .weightOral(1.0)
                            .status(CertificationStatus.PUBLISHED)
                            .build()
            );

            LocalDateTime now = LocalDateTime.now();

            OralSession plannedJava = oralSessionRepository.save(
                    OralSession.builder()
                            .certification(javaCatalog)
                            .title("Java Oral Session A")
                            .scheduledAt(now.plusDays(2))
                            .durationMinutes(45)
                            .meetingProvider(MeetingProvider.MEET)
                            .meetingLink("https://meet.google.com/demo-java-a")
                            .evaluatorId(9001L)
                            .status(OralSessionStatus.PLANNED)
                            .build()
            );

            OralSession plannedAngular = oralSessionRepository.save(
                    OralSession.builder()
                            .certification(angularCatalog)
                            .title("Angular Oral Session B")
                            .scheduledAt(now.plusDays(4))
                            .durationMinutes(50)
                            .meetingProvider(MeetingProvider.TEAMS)
                            .meetingLink("https://teams.microsoft.com/l/demo-angular-b")
                            .evaluatorId(9002L)
                            .status(OralSessionStatus.PLANNED)
                            .build()
            );

            OralSession doneJava = oralSessionRepository.save(
                    OralSession.builder()
                            .certification(javaCatalog)
                            .title("Java Oral Session (Completed)")
                            .scheduledAt(now.minusDays(7))
                            .durationMinutes(45)
                            .meetingProvider(MeetingProvider.MEET)
                            .meetingLink("https://meet.google.com/demo-java-done")
                            .evaluatorId(9001L)
                            .status(OralSessionStatus.DONE)
                            .build()
            );

            OralSession doneAngular = oralSessionRepository.save(
                    OralSession.builder()
                            .certification(angularCatalog)
                            .title("Angular Oral Session (Completed)")
                            .scheduledAt(now.minusDays(5))
                            .durationMinutes(50)
                            .meetingProvider(MeetingProvider.TEAMS)
                            .meetingLink("https://teams.microsoft.com/l/demo-angular-done")
                            .evaluatorId(9002L)
                            .status(OralSessionStatus.DONE)
                            .build()
            );

            OralSession plannedJavaEvening = oralSessionRepository.save(
                    OralSession.builder()
                            .certification(javaCatalog)
                            .title("Java Oral Session C (Evening)")
                            .scheduledAt(now.plusDays(6).withHour(18).withMinute(0))
                            .durationMinutes(40)
                            .meetingProvider(MeetingProvider.MEET)
                            .meetingLink("https://meet.google.com/demo-java-evening")
                            .evaluatorId(9003L)
                            .status(OralSessionStatus.PLANNED)
                            .build()
            );

            OralSession doneAngularRetake = oralSessionRepository.save(
                    OralSession.builder()
                            .certification(angularCatalog)
                            .title("Angular Oral Session (Retake)")
                            .scheduledAt(now.minusDays(12))
                            .durationMinutes(50)
                            .meetingProvider(MeetingProvider.TEAMS)
                            .meetingLink("https://teams.microsoft.com/l/demo-angular-retake")
                            .evaluatorId(9002L)
                            .status(OralSessionStatus.DONE)
                            .build()
            );

            OralExamAssignment assigned = assignmentRepository.save(
                    OralExamAssignment.builder()
                            .oralSession(plannedJava)
                            .learnerId(2001L)
                            .formationId(1L)
                            .status(AssignmentStatus.ASSIGNED)
                            .attemptNumber(1)
                            .build()
            );

            OralExamAssignment rescheduleRequested = assignmentRepository.save(
                    OralExamAssignment.builder()
                            .oralSession(plannedAngular)
                            .learnerId(2002L)
                            .formationId(2L)
                            .status(AssignmentStatus.RESCHEDULE_REQUESTED)
                            .attemptNumber(1)
                            .build()
            );

            OralExamAssignment assignedSecondBatch = assignmentRepository.save(
                    OralExamAssignment.builder()
                            .oralSession(plannedJavaEvening)
                            .learnerId(2006L)
                            .formationId(1L)
                            .status(AssignmentStatus.ASSIGNED)
                            .attemptNumber(1)
                            .build()
            );

            OralExamAssignment completedNoIssue = assignmentRepository.save(
                    OralExamAssignment.builder()
                            .oralSession(doneJava)
                            .learnerId(2003L)
                            .formationId(1L)
                            .status(AssignmentStatus.COMPLETED)
                            .oralScore(86.0)
                            .attemptNumber(1)
                            .gradedAt(now.minusDays(6))
                            .evaluatorComment("Strong technical depth and clear communication")
                            .build()
            );

            OralExamAssignment failedAfterTwo = assignmentRepository.save(
                    OralExamAssignment.builder()
                            .oralSession(doneJava)
                            .learnerId(2004L)
                            .formationId(1L)
                            .status(AssignmentStatus.FAILED)
                            .oralScore(42.0)
                            .attemptNumber(2)
                            .gradedAt(now.minusDays(4))
                            .evaluatorComment("Insufficient command over core concepts after retake")
                            .build()
            );

            OralExamAssignment completedRetakePass = assignmentRepository.save(
                    OralExamAssignment.builder()
                            .oralSession(doneAngularRetake)
                            .learnerId(2007L)
                            .formationId(2L)
                            .status(AssignmentStatus.COMPLETED)
                            .oralScore(78.0)
                            .attemptNumber(2)
                            .gradedAt(now.minusDays(11))
                            .evaluatorComment("Clear improvement on architecture and testing topics")
                            .build()
            );

            OralExamAssignment noShowAssignment = assignmentRepository.save(
                    OralExamAssignment.builder()
                            .oralSession(doneJava)
                            .learnerId(2008L)
                            .formationId(1L)
                            .status(AssignmentStatus.NO_SHOW)
                            .attemptNumber(1)
                            .gradedAt(now.minusDays(7))
                            .evaluatorComment("Learner did not attend scheduled oral session")
                            .build()
            );

            OralExamAssignment completedIssued = assignmentRepository.save(
                    OralExamAssignment.builder()
                            .oralSession(doneAngular)
                            .learnerId(2005L)
                            .formationId(2L)
                            .status(AssignmentStatus.COMPLETED)
                            .oralScore(91.0)
                            .attemptNumber(1)
                            .gradedAt(now.minusDays(3))
                            .evaluatorComment("Excellent frontend architecture decisions")
                            .build()
            );

            rescheduleRequestRepository.saveAll(List.of(
                    RescheduleRequest.builder()
                            .assignment(rescheduleRequested)
                            .proposedDatetime(now.plusDays(6))
                            .message("Conflict with work schedule, requesting a new slot")
                            .status(RescheduleStatus.PENDING)
                            .build(),
                    RescheduleRequest.builder()
                            .assignment(assigned)
                            .proposedDatetime(now.plusDays(3))
                            .message("Need a slightly later slot")
                            .status(RescheduleStatus.APPROVED)
                            .decidedAt(now.minusDays(1))
                            .adminComment("Approved and rescheduled")
                            .build(),
                    RescheduleRequest.builder()
                            .assignment(assignedSecondBatch)
                            .proposedDatetime(now.plusDays(7).withHour(17).withMinute(30))
                            .message("University exam conflict, asking for +1 day")
                            .status(RescheduleStatus.REJECTED)
                            .decidedAt(now.minusDays(1))
                            .adminComment("Rejected: evaluator unavailable at requested time")
                            .build()
            ));

            issuedCertificationRepository.saveAll(List.of(
                    IssuedCertification.builder()
                            .learnerId(completedIssued.getLearnerId())
                            .certification(angularCatalog)
                            .formationId(completedIssued.getFormationId())
                            .writtenScore(0.0)
                            .oralScore(completedIssued.getOralScore())
                            .finalScore(completedIssued.getOralScore())
                            .issuedAt(now.minusDays(2))
                            .expiresAt(now.plusMonths(24))
                            .certificateNumber("FORME-DEMO-0001")
                            .status(IssuedCertificationStatus.ISSUED)
                            .pdfPath("generated-certificates/FORME-DEMO-0001.pdf")
                            .build(),
                    IssuedCertification.builder()
                            .learnerId(completedRetakePass.getLearnerId())
                            .certification(angularCatalog)
                            .formationId(completedRetakePass.getFormationId())
                            .writtenScore(0.0)
                            .oralScore(completedRetakePass.getOralScore())
                            .finalScore(completedRetakePass.getOralScore())
                            .issuedAt(now.minusDays(9))
                            .expiresAt(now.plusMonths(24))
                            .certificateNumber("FORME-DEMO-0002")
                            .status(IssuedCertificationStatus.ISSUED)
                            .pdfPath("generated-certificates/FORME-DEMO-0002.pdf")
                            .build(),
                    IssuedCertification.builder()
                            .learnerId(2010L)
                            .certification(javaCatalog)
                            .formationId(1L)
                            .writtenScore(0.0)
                            .oralScore(74.0)
                            .finalScore(74.0)
                            .issuedAt(now.minusMonths(2))
                            .expiresAt(now.plusMonths(22))
                            .certificateNumber("FORME-DEMO-0003")
                            .status(IssuedCertificationStatus.REVOKED)
                            .pdfPath("generated-certificates/FORME-DEMO-0003.pdf")
                            .build()
            ));

            // keep references to avoid accidental optimization/removal in future edits
            if (completedNoIssue.getId() == null || failedAfterTwo.getId() == null || noShowAssignment.getId() == null) {
                throw new IllegalStateException("Demo assignments were not persisted correctly");
            }
        };
    }
}
