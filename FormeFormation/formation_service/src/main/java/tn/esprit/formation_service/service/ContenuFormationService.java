package tn.esprit.formation_service.service;

import tn.esprit.formation_service.entity.ContenuFormation;

import java.util.List;
import java.util.Optional;

public interface ContenuFormationService {

    ContenuFormation save(ContenuFormation contenuFormation);
    Optional<ContenuFormation> findById(Long id);
    List<ContenuFormation> findAll();
    List<ContenuFormation> findByFormationId(Long formationId);
    void deleteById(Long id);
    ContenuFormation update(Long id, ContenuFormation contenuFormation);
}
