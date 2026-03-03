package tn.esprit.formation_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.formation_service.entity.ContenuFormation;

import java.util.List;
import java.util.Optional;

public interface ContenuFormationRepository extends JpaRepository<ContenuFormation, Long> {

    @Query("SELECT DISTINCT c FROM ContenuFormation c LEFT JOIN FETCH c.evaluation WHERE c.formation.id = :formationId ORDER BY c.order_index ASC")
    List<ContenuFormation> findByFormationIdOrderByOrder_indexAsc(@Param("formationId") Long formationId);

    @Query("SELECT c FROM ContenuFormation c WHERE c.formation.id = :formationId AND c.evaluation.id = :evaluationId")
    Optional<ContenuFormation> findByFormation_IdAndEvaluation_Id(@Param("formationId") Long formationId, @Param("evaluationId") Long evaluationId);

    @Query("SELECT c FROM ContenuFormation c WHERE c.formation.id = :formationId AND c.order_index > :orderIndex")
    List<ContenuFormation> findByFormation_IdAndOrder_indexGreaterThan(@Param("formationId") Long formationId, @Param("orderIndex") Integer orderIndex);

    @Query("SELECT c FROM ContenuFormation c WHERE c.formation.id = :formationId AND c.order_index = :orderIndex")
    Optional<ContenuFormation> findByFormation_IdAndOrder_index(@Param("formationId") Long formationId, @Param("orderIndex") Integer orderIndex);
}
