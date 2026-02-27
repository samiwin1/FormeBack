package tn.esprit.formation_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.formation_service.entity.Evaluation;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    List<Evaluation> findByFormation_Id(Long formationId);
}
