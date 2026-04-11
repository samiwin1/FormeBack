package tn.esprit.mentorservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.mentorservice.entity.ConversationMessage;

import java.util.List;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    List<ConversationMessage> findByThreadIdOrderByCreatedAtAsc(Long threadId);

    long countByThreadId(Long threadId);
}
