package tn.esprit.mentorservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.mentorservice.entity.MentorNotification;

import java.util.List;

@Repository
public interface MentorNotificationRepository extends JpaRepository<MentorNotification, Long> {

    // ── Admin broadcast (targetUserId IS NULL) ──────────────────────────────

    List<MentorNotification> findByTargetUserIdIsNullOrderByCreatedAtDesc();

    long countByTargetUserIdIsNullAndReadFalse();

    @Modifying
    @Query("UPDATE MentorNotification n SET n.read = true WHERE n.targetUserId IS NULL AND n.read = false")
    void markAllAdminRead();

    // ── Learner-specific (targetUserId = userId) ────────────────────────────

    List<MentorNotification> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId);

    long countByTargetUserIdAndReadFalse(Long targetUserId);

    @Modifying
    @Query("UPDATE MentorNotification n SET n.read = true WHERE n.targetUserId = :uid AND n.read = false")
    void markAllUserRead(@Param("uid") Long userId);
}
