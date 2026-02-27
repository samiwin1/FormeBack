package tn.esprit.formation_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.formation_service.entity.ContenuFormation;

import java.util.List;

public interface ContenuFormationRepository extends JpaRepository<ContenuFormation, Long> {

    @Query("SELECT DISTINCT c FROM ContenuFormation c LEFT JOIN FETCH c.evaluation WHERE c.formation.id = :formationId ORDER BY c.order_index ASC")
    List<ContenuFormation> findByFormationIdOrderByOrder_indexAsc(@Param("formationId") Long formationId);
}
