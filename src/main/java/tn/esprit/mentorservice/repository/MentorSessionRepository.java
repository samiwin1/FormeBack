package tn.esprit.mentorservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.mentorservice.domain.MentorChannel;
import tn.esprit.mentorservice.entity.MentorSession;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MentorSessionRepository extends JpaRepository<MentorSession, Long> {

    Optional<MentorSession> findFirstByUserIdAndChannelAndPeriodStartOrderByCreatedAtDesc(
            Long userId, MentorChannel channel, LocalDate periodStart);

    @Query("""
            SELECT s FROM MentorSession s
            WHERE s.userId = :userId
            ORDER BY s.createdAt DESC
            """)
    Page<MentorSession> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(s) FROM MentorSession s WHERE s.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT s.channel, COUNT(s) FROM MentorSession s WHERE s.userId = :userId GROUP BY s.channel")
    List<Object[]> countByUserIdGroupedByChannel(@Param("userId") Long userId);

    @Query("SELECT MAX(s.createdAt) FROM MentorSession s WHERE s.userId = :userId")
    Optional<Instant> findLastSessionAtByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT DATE(created_at) as day, COUNT(*) as cnt " +
                   "FROM mentor_session GROUP BY DATE(created_at) ORDER BY day DESC LIMIT 30",
           nativeQuery = true)
    List<Object[]> dailyUsage();

    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m') as month, COUNT(*) as cnt " +
                   "FROM mentor_session GROUP BY month ORDER BY month DESC LIMIT 12",
           nativeQuery = true)
    List<Object[]> monthlyUsage();
}
