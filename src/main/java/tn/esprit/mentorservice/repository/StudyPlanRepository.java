package tn.esprit.mentorservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.mentorservice.entity.StudyPlan;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
    List<StudyPlan> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT COUNT(sp) FROM StudyPlan sp WHERE sp.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT sp.title, MAX(sp.createdAt) FROM StudyPlan sp WHERE sp.userId = :userId GROUP BY sp.title ORDER BY MAX(sp.createdAt) DESC")
    Optional<Object[]> findLatestPlanTitleAndDateByUserId(@Param("userId") Long userId);
}
