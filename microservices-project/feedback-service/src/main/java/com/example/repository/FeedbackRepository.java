package com.example.repository;

import com.example.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByPartnerId(Long partnerId);
    List<Feedback> findByPackId(Long packId);
    List<Feedback> findByDealId(Long dealId);
    List<Feedback> findByStatus(String status);
    List<Feedback> findByRating(Integer rating);
    List<Feedback> findBySentiment(String sentiment);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.status = 'APPROVED'")
    Double getGlobalAverageRating();

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.packId = :packId AND f.status = 'APPROVED'")
    Double getAverageRatingByPack(Long packId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.partnerId = :partnerId")
    Double getAverageRatingByPartner(Long partnerId);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.status = :status")
    Long countByStatus(String status);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.sentiment = :sentiment")
    Long countBySentiment(String sentiment);
}
