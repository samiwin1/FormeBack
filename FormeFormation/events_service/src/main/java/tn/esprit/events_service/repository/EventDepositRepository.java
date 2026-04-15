package tn.esprit.events_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.events_service.entity.EventDeposit;

import java.util.Optional;

public interface EventDepositRepository extends JpaRepository<EventDeposit, Long> {

    Optional<EventDeposit> findByParticipantId(Long participantId);

    @Query("SELECT d FROM EventDeposit d WHERE d.id = :depositId AND d.participant.event.id = :eventId")
    Optional<EventDeposit> findByIdAndEventId(@Param("depositId") Long depositId, @Param("eventId") Long eventId);

    @Query("SELECT d FROM EventDeposit d JOIN FETCH d.participant p JOIN FETCH p.event WHERE d.id = :id")
    Optional<EventDeposit> findByIdWithParticipantAndEvent(@Param("id") Long id);
}
