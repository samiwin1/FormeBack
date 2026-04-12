package tn.esprit.forme.certificationservice.domain.enums;

public enum AssignmentStatus {
    ASSIGNED,
    RESCHEDULE_REQUESTED,
    @Deprecated
    REQUESTED_RESCHEDULE,
    RESCHEDULED,
    COMPLETED,
    FAILED,
    CANCELLED,
    NO_SHOW
}
