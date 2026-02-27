package tn.esprit.formation_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.formation_service.entity.Examen;

import java.util.Optional;

public interface ExamenRepository extends JpaRepository<Examen, Long> {

    Optional<Examen> findByFormation_Id(Long formationId);
}
