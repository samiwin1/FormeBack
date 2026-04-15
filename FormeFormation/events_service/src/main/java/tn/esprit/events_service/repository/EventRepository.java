package tn.esprit.events_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.events_service.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}
