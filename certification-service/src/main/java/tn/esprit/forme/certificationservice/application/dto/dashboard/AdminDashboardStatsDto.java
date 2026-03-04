package tn.esprit.forme.certificationservice.application.dto.dashboard;

public record AdminDashboardStatsDto(
        long totalCertifications,
        long oralSessionsPlanned,
        long learnersAssigned,
        long pendingReschedules,
        long issuedCertifications
) {
}
