package tn.esprit.forme.certificationservice.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.forme.certificationservice.domain.entity.ReminderLog;

public interface ReminderLogRepository extends JpaRepository<ReminderLog, Long> {
    // For SessionReminderService (24h reminder)
    boolean existsByAssignmentIdAndReminderType(Long assignmentId, String reminderType);
    
    // For SmartReminderService (J-7, J-3, J-1, H-2)
    boolean existsBySessionIdAndLearnerIdAndStage(Long sessionId, Long learnerId, String stage);
}
