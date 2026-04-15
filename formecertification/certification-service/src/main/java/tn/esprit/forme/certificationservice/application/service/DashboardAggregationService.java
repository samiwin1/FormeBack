package tn.esprit.forme.certificationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.forme.certificationservice.application.dto.dashboard.*;
import tn.esprit.forme.certificationservice.domain.entity.OralExamAssignment;
import tn.esprit.forme.certificationservice.domain.enums.IssuedCertificationStatus;
import tn.esprit.forme.certificationservice.domain.enums.AssignmentStatus;
import tn.esprit.forme.certificationservice.domain.enums.OralSessionStatus;
import tn.esprit.forme.certificationservice.domain.enums.RescheduleStatus;
import tn.esprit.forme.certificationservice.domain.repository.CertificationCatalogRepository;
import tn.esprit.forme.certificationservice.domain.repository.IssuedCertificationRepository;
import tn.esprit.forme.certificationservice.domain.repository.OralExamAssignmentRepository;
import tn.esprit.forme.certificationservice.domain.repository.OralSessionRepository;
import tn.esprit.forme.certificationservice.domain.repository.RescheduleRequestRepository;
import tn.esprit.forme.certificationservice.infrastructure.feign.FormationClient;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor

public class DashboardAggregationService {

    private final EligibilityService eligibilityService;
    private final OralExamAssignmentRepository assignmentRepository;
    private final IssuedCertificationRepository issuedCertificationRepository;
    private final CertificationCatalogRepository certificationCatalogRepository;
    private final OralSessionRepository oralSessionRepository;
    private final RescheduleRequestRepository rescheduleRequestRepository;
    private final UserDirectoryAggregationService userDirectoryAggregationService;
    private final FormationClient formationClient;
    private final ScoringService scoringService;

    @Transactional(readOnly = true)
    public List<EligibleLearnerDto> getEligibleLearnersForOral(Long formationId) {
        String formationTitle = resolveFormationTitle(formationId);
        Set<Long> alreadyAssigned = assignmentRepository.findAll().stream()
                .filter(a -> formationId != null && formationId.equals(a.getFormationId()))
                .map(OralExamAssignment::getLearnerId)
                .filter(Objects::nonNull)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);

        var passedLearners = eligibilityService.getPassedLearnersForFormation(formationId);
        Set<Long> learnerIds = new HashSet<>();
        passedLearners.forEach(r -> learnerIds.add(r.getLearnerId()));
        Map<Long, String> namesById = userDirectoryAggregationService.resolveDisplayNames(new ArrayList<>(learnerIds));

        return passedLearners.stream()
                .filter(r -> !alreadyAssigned.contains(r.getLearnerId()))
                .map(r -> new EligibleLearnerDto(
                        r.getLearnerId(),
                        namesById.getOrDefault(r.getLearnerId(), fallbackUserName(r.getLearnerId())),
                        formationId,
                        formationTitle,
                        r.getScore(),
                        r.getPassed()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PendingOralEvaluationDto> getPendingOralEvaluations() {
        List<OralExamAssignment> assignments = assignmentRepository.findAll().stream()
                .filter(a -> a.getOralScore() == null)
                .filter(a -> a.getStatus() != AssignmentStatus.COMPLETED)
                .toList();

        Set<Long> userIds = new HashSet<>();
        assignments.forEach(a -> {
            userIds.add(a.getLearnerId());
            if (a.getOralSession() != null) {
                userIds.add(a.getOralSession().getEvaluatorId());
            }
        });
        Map<Long, String> namesById = userDirectoryAggregationService.resolveDisplayNames(new ArrayList<>(userIds));

        return assignments.stream()
                .map(a -> new PendingOralEvaluationDto(
                        a.getId(),
                        a.getLearnerId(),
                        namesById.getOrDefault(a.getLearnerId(), fallbackUserName(a.getLearnerId())),
                        a.getAttemptNumber(),
                        a.getOralSession().getId(),
                        a.getOralSession().getCertification() == null
                                ? null
                                : a.getOralSession().getCertification().getTitle(),
                        a.getOralSession().getScheduledAt(),
                        a.getOralSession().getMeetingLink(),
                        a.getOralSession().getEvaluatorId(),
                        namesById.getOrDefault(a.getOralSession().getEvaluatorId(), fallbackUserName(a.getOralSession().getEvaluatorId())),
                        a.getStatus().name()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PassedOralWithoutCertificateDto> getPassedOralWithoutCertificate() {
        List<OralExamAssignment> assignments = assignmentRepository.findAll().stream()
                .filter(a -> a.getStatus() == AssignmentStatus.COMPLETED)
                .filter(a -> a.getOralScore() != null)
                .filter(a -> a.getFormationId() != null)
                .filter(a -> !issuedCertificationRepository.existsByLearnerIdAndCertificationIdAndFormationId(
                        a.getLearnerId(),
                        a.getOralSession().getCertification().getId(),
                        a.getFormationId()
                ))
                .toList();

        Set<Long> learnerIds = new HashSet<>();
        assignments.forEach(a -> learnerIds.add(a.getLearnerId()));
        Map<Long, String> namesById = userDirectoryAggregationService.resolveDisplayNames(new ArrayList<>(learnerIds));

        return assignments.stream()
                .map(a -> {
                    var catalog = a.getOralSession().getCertification();
                    var writtenResult = eligibilityService.getWrittenExamResult(a.getLearnerId(), a.getFormationId());
                    double writtenScore = writtenResult != null && writtenResult.getScore() != null ? writtenResult.getScore() : 0.0;
                    double finalScore = scoringService.computeFinalScore(
                            writtenScore,
                            a.getOralScore(),
                            catalog.getWeightWritten(),
                            catalog.getWeightOral()
                    );
                    return new PassedOralWithoutCertificateDto(
                            a.getId(),
                            a.getLearnerId(),
                            namesById.getOrDefault(a.getLearnerId(), fallbackUserName(a.getLearnerId())),
                            a.getOralSession().getId(),
                            catalog.getId(),
                            a.getFormationId(),
                            a.getOralScore(),
                            writtenScore,
                            finalScore,
                            a.getStatus().name()
                    );
                })
                .filter(dto -> {
                    var catalog = certificationCatalogRepository.findById(dto.certificationId()).orElse(null);
                    return catalog != null && dto.finalScore() >= catalog.getThresholdFinal();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FailedOralAttemptDto> getFailedAfterTwoAttempts() {
        List<OralExamAssignment> assignments = assignmentRepository.findAll().stream()
                .filter(a -> a.getStatus() == AssignmentStatus.FAILED)
                .toList();

        Set<Long> learnerIds = new HashSet<>();
        assignments.forEach(a -> learnerIds.add(a.getLearnerId()));
        Map<Long, String> namesById = userDirectoryAggregationService.resolveDisplayNames(new ArrayList<>(learnerIds));

        return assignments.stream()
                .map(a -> new FailedOralAttemptDto(
                        a.getId(),
                        a.getLearnerId(),
                        namesById.getOrDefault(a.getLearnerId(), fallbackUserName(a.getLearnerId())),
                        a.getOralSession().getId(),
                        a.getOralSession().getCertification().getId(),
                        a.getFormationId(),
                        a.getAttemptNumber(),
                        a.getOralScore(),
                        a.getStatus().name()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public MyExamStatusDto getMyExamStatus(Long learnerId, Long formationId) {
        OralExamAssignment latest = assignmentRepository.findByLearnerId(learnerId).stream()
                .filter(a -> formationId == null || formationId.equals(a.getFormationId()))
                .max(Comparator.comparing(OralExamAssignment::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);

        Long resolvedFormationId = formationId;
        if (resolvedFormationId == null && latest != null) {
            resolvedFormationId = latest.getFormationId();
        }

        Double writtenScore = null;
        Boolean writtenPassed = null;
        if (resolvedFormationId != null) {
            var written = eligibilityService.getWrittenExamResult(learnerId, resolvedFormationId);
            if (written != null) {
                writtenScore = written.getScore();
                writtenPassed = written.getPassed();
            }
        }

        if (latest == null) {
            return new MyExamStatusDto(resolvedFormationId, writtenScore, writtenPassed,
                    "NOT_ASSIGNED", null, null, null);
        }

        String oralStatus;
        if (latest.getStatus() == AssignmentStatus.COMPLETED) {
            oralStatus = "COMPLETED";
        } else if (latest.getStatus() == AssignmentStatus.FAILED) {
            oralStatus = "FAILED";
        } else {
            oralStatus = "ASSIGNED";
        }
        return new MyExamStatusDto(
                resolvedFormationId,
                writtenScore,
                writtenPassed,
                oralStatus,
                latest.getOralSession().getScheduledAt(),
                latest.getOralSession().getMeetingLink(),
                latest.getOralScore()
        );
    }

    @Transactional(readOnly = true)
    public MyCertificationStatusDto getMyCertificationStatus(Long learnerId, Long formationId) {
        var issued = issuedCertificationRepository.findByLearnerId(learnerId).stream()
                .filter(c -> formationId == null || formationId.equals(c.getFormationId()))
                .max(Comparator.comparing(c -> c.getIssuedAt(), Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);

        if (issued != null) {
            return new MyCertificationStatusDto(
                    "PASSED",
                    issued.getId(),
                    issued.getCertificateNumber(),
                    issued.getFinalScore(),
                    issued.getPdfPath(),
                    true
            );
        }

        OralExamAssignment latest = assignmentRepository.findByLearnerId(learnerId).stream()
                .filter(a -> formationId == null || formationId.equals(a.getFormationId()))
                .max(Comparator.comparing(OralExamAssignment::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);

        if (latest != null && latest.getStatus() == AssignmentStatus.FAILED) {
            return new MyCertificationStatusDto("FAILED", null, null, latest.getOralScore(), null, false);
        }

        if (latest == null || latest.getStatus() != AssignmentStatus.COMPLETED || latest.getOralScore() == null) {
            return new MyCertificationStatusDto("IN_PROGRESS", null, null, null, null, false);
        }

        Double writtenScore = null;
        if (latest.getFormationId() != null) {
            var written = eligibilityService.getWrittenExamResult(learnerId, latest.getFormationId());
            if (written != null) {
                writtenScore = written.getScore();
            }
        }

        if (writtenScore == null) {
            return new MyCertificationStatusDto("IN_PROGRESS", null, null, null, null, false);
        }

        double finalScore = scoringService.computeFinalScore(
                writtenScore,
                latest.getOralScore(),
                latest.getOralSession().getCertification().getWeightWritten(),
                latest.getOralSession().getCertification().getWeightOral()
        );

        boolean passed = finalScore >= latest.getOralSession().getCertification().getThresholdFinal();
        return new MyCertificationStatusDto(passed ? "PASSED" : "FAILED", null, null, finalScore, null, false);
    }

    @Transactional(readOnly = true)
    public long countPendingReschedules() {
        return rescheduleRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() != null && r.getStatus().name().equals("PENDING"))
                .count();
    }

    @Transactional(readOnly = true)
    public AdminDashboardStatsDto getAdminStats(Long formationId) {
        long totalCertifications = certificationCatalogRepository.count();
        long oralSessionsPlanned = oralSessionRepository.countByStatus(OralSessionStatus.PLANNED);

        List<OralExamAssignment> assignments = assignmentRepository.findAll();
        long learnersAssigned = assignments.stream()
                .filter(a -> a.getStatus() == AssignmentStatus.ASSIGNED)
                .filter(a -> formationId == null || Objects.equals(a.getFormationId(), formationId))
                .count();

        long pendingReschedules = rescheduleRequestRepository.countByStatus(RescheduleStatus.PENDING);
        long issuedCertifications;
        if (formationId == null) {
            issuedCertifications = issuedCertificationRepository.countByStatus(IssuedCertificationStatus.ISSUED);
        } else {
            issuedCertifications = issuedCertificationRepository.findByFormationIdAndStatus(
                    formationId, IssuedCertificationStatus.ISSUED
            ).size();
        }

        return new AdminDashboardStatsDto(
                totalCertifications,
                oralSessionsPlanned,
                learnersAssigned,
                pendingReschedules,
                issuedCertifications
        );
    }

    @Transactional(readOnly = true)
    public List<RescheduleAdminItemDto> getPendingRescheduleRequests() {
        return getRescheduleRequests(RescheduleStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<RescheduleAdminItemDto> getRescheduleRequests(RescheduleStatus status) {
        var requests = status == null
                ? rescheduleRequestRepository.findAll()
                : rescheduleRequestRepository.findByStatus(status);

        Set<Long> learnerIds = new HashSet<>();
        requests.forEach(item -> learnerIds.add(item.getAssignment().getLearnerId()));
        Map<Long, String> namesById = userDirectoryAggregationService.resolveDisplayNames(new ArrayList<>(learnerIds));

        return requests.stream()
                .sorted(Comparator.comparing(r -> r.getRequestedAt(), Comparator.nullsLast(Comparator.reverseOrder())))
                .map(item -> {
                    var assignment = item.getAssignment();
                    var session = assignment.getOralSession();
                    Long learnerId = assignment.getLearnerId();
                    return new RescheduleAdminItemDto(
                            item.getId(),
                            assignment.getId(),
                            learnerId,
                            namesById.getOrDefault(learnerId, fallbackUserName(learnerId)),
                            session.getId(),
                            session.getScheduledAt(),
                            item.getProposedDatetime(),
                            item.getMessage(),
                            item.getRequestedAt(),
                            item.getStatus().name()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<tn.esprit.forme.certificationservice.application.dto.reschedule.RescheduleResponse> getMyRescheduleRequests(Long learnerId) {
        return rescheduleRequestRepository.findByAssignmentLearnerIdOrderByRequestedAtDesc(learnerId).stream()
                .map(item -> new tn.esprit.forme.certificationservice.application.dto.reschedule.RescheduleResponse(
                        item.getId(),
                        item.getAssignment().getId(),
                        item.getProposedDatetime(),
                        item.getMessage(),
                        item.getRequestedAt(),
                        item.getDecidedAt(),
                        item.getAdminComment(),
                        item.getStatus()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public EvaluatorOverviewDto getEvaluatorOverview(Long evaluatorId) {
        var sessions = oralSessionRepository.findByEvaluatorId(evaluatorId);
        LocalDate today = LocalDate.now();
        var todaySessions = sessions.stream()
                .filter(s -> s.getScheduledAt() != null && s.getScheduledAt().toLocalDate().equals(today))
                .sorted(Comparator.comparing(s -> s.getScheduledAt(), Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        int sessionsTodayCount = todaySessions.size();
        int learnersToEvaluateCount = assignmentRepository.findByOralSessionEvaluatorId(evaluatorId).stream()
                .filter(a -> a.getStatus() != AssignmentStatus.COMPLETED && a.getStatus() != AssignmentStatus.NO_SHOW)
                .map(OralExamAssignment::getId)
                .toList()
                .size();

        List<EvaluatorOverviewDto.SessionTodayItemDto> sessionsToday = todaySessions.stream()
                .map(s -> new EvaluatorOverviewDto.SessionTodayItemDto(
                        s.getId(),
                        s.getTitle(),
                        s.getScheduledAt(),
                        (int) assignmentRepository.countByOralSessionId(s.getId())
                ))
                .toList();

        return new EvaluatorOverviewDto(
                sessionsTodayCount,
                learnersToEvaluateCount,
                sessionsToday
        );
    }

    private String resolveFormationTitle(Long formationId) {
        if (formationId == null) {
            return null;
        }
        try {
            var formation = formationClient.getFormationById(formationId);
            if (formation != null && formation.getTitle() != null && !formation.getTitle().isBlank()) {
                return formation.getTitle();
            }
        } catch (Exception ignored) {
            // best-effort only
        }
        return "Formation #" + formationId;
    }

    private String fallbackUserName(Long userId) {
        return userId == null ? null : "User #" + userId;
    }
}
