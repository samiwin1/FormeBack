package tn.esprit.mentorservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.mentorservice.entity.ConversationThread;

import java.util.List;
import java.util.Optional;

public interface ConversationThreadRepository extends JpaRepository<ConversationThread, Long> {

    List<ConversationThread> findByUserIdAndIsActiveTrueOrderByUpdatedAtDesc(Long userId);

    Optional<ConversationThread> findByIdAndUserId(Long id, Long userId);
}
