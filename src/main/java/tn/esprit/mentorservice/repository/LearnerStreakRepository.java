package tn.esprit.mentorservice.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.mentorservice.entity.LearnerStreak;

import java.util.List;
import java.util.Optional;

public interface LearnerStreakRepository extends JpaRepository<LearnerStreak, Long> {
    Optional<LearnerStreak> findByUserId(Long userId);

    @Query("SELECT s FROM LearnerStreak s ORDER BY s.currentStreak DESC")
    List<LearnerStreak> findTopByStreak(Pageable pageable);
}
