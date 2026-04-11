package tn.esprit.mentorservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.mentorservice.domain.FeedbackRating;
import tn.esprit.mentorservice.entity.MentorFeedback;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MentorFeedbackRepository extends JpaRepository<MentorFeedback, Long> {

    Optional<MentorFeedback> findBySessionIdAndUserId(Long sessionId, Long userId);

    List<MentorFeedback> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndRating(Long userId, FeedbackRating rating);

    /** Last N DOWN-rated feedbacks with a non-blank comment for prompt context. */
    List<MentorFeedback> findTop3ByUserIdAndRatingOrderByCreatedAtDesc(Long userId, FeedbackRating rating);

    @Query("SELECT MAX(f.createdAt) FROM MentorFeedback f WHERE f.userId = :userId")
    Optional<Instant> findLastFeedbackAtByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT rating, COUNT(*) as cnt FROM mentor_feedback GROUP BY rating",
           nativeQuery = true)
    List<Object[]> countByRating();
}
