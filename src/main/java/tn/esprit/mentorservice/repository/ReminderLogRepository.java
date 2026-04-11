package tn.esprit.mentorservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.mentorservice.entity.ReminderLog;

import java.time.LocalDate;

public interface ReminderLogRepository extends JpaRepository<ReminderLog, Long> {
    boolean existsByUserIdAndSentDateAndReminderType(Long userId, LocalDate sentDate, String reminderType);
}
