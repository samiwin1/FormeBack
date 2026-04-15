package tn.esprit.events_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.events_service.entity.EventParticipant;

import java.util.List;
import java.util.Optional;

public interface EventParticipantRepository extends JpaRepository<EventParticipant, Long> {

    Optional<EventParticipant> findByEventIdAndUserId(Long eventId, Long userId);

    List<EventParticipant> findByEventId(Long eventId);

    long countByEventId(Long eventId);
}
