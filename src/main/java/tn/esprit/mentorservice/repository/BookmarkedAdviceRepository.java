package tn.esprit.mentorservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.mentorservice.entity.BookmarkedAdvice;

import java.util.Optional;
import java.util.Set;

public interface BookmarkedAdviceRepository extends JpaRepository<BookmarkedAdvice, Long> {

    Optional<BookmarkedAdvice> findByUserIdAndSessionId(Long userId, Long sessionId);

    boolean existsByUserIdAndSessionId(Long userId, Long sessionId);

    @Query(
        value = "SELECT b FROM BookmarkedAdvice b JOIN FETCH b.session WHERE b.userId = :userId ORDER BY b.createdAt DESC",
        countQuery = "SELECT COUNT(b) FROM BookmarkedAdvice b WHERE b.userId = :userId"
    )
    Page<BookmarkedAdvice> findPageByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT b.session.id FROM BookmarkedAdvice b WHERE b.userId = :userId")
    Set<Long> findSessionIdsByUserId(@Param("userId") Long userId);
}
