package com.example.repository;

import com.example.entity.FeedbackResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FeedbackResponseRepository extends JpaRepository<FeedbackResponse, Long> {
    Optional<FeedbackResponse> findByFeedbackId(Long feedbackId);
    boolean existsByFeedbackId(Long feedbackId);
}
