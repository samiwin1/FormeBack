package tn.esprit.formation_service.service;

import tn.esprit.formation_service.dto.ExamHistoryItem;
import tn.esprit.formation_service.entity.Examen;

import java.util.List;
import java.util.Optional;

public interface ExamenService {

    Examen save(Examen examen);
    Optional<Examen> findById(Long id);
    Optional<Examen> findByFormationId(Long formationId);
    List<Examen> findAll();
    void deleteById(Long id);
    Examen update(Long id, Examen examen);

    List<ExamHistoryItem> getExamHistory(Long userId, Long formationId);
}
