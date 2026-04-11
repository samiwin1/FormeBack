package tn.esprit.mentorservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.mentorservice.entity.MentorAdvice;

import java.util.Optional;

public interface MentorAdviceRepository extends JpaRepository<MentorAdvice, Long> {

    Optional<MentorAdvice> findBySessionId(Long sessionId);

    /**
     * Explicit JPQL + count query avoids failures from combining {@code @EntityGraph} with
     * {@link Page} and nested sort paths. Session is loaded lazily within the service transaction.
     */
    @Query(
            value = "SELECT a FROM MentorAdvice a WHERE a.session.userId = :userId ORDER BY a.session.createdAt DESC",
            countQuery = "SELECT COUNT(a) FROM MentorAdvice a WHERE a.session.userId = :userId"
    )
    Page<MentorAdvice> findPageByUserId(@Param("userId") Long userId, Pageable pageable);
}
