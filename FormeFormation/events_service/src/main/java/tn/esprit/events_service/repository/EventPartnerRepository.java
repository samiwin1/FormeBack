package tn.esprit.events_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.events_service.entity.EventPartner;
import tn.esprit.events_service.entity.EventTier;

import java.util.List;
import java.util.Optional;

public interface EventPartnerRepository extends JpaRepository<EventPartner, Long> {

    long countByEventIdAndTier(Long eventId, EventTier tier);

    List<EventPartner> findByEventId(Long eventId);

    Optional<EventPartner> findByEventIdAndPartnerId(Long eventId, Long partnerId);

    boolean existsByEventIdAndPartnerId(Long eventId, Long partnerId);
}
