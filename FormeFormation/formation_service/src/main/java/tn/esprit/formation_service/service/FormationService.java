package tn.esprit.formation_service.service;

import tn.esprit.formation_service.entity.Formation;

import java.util.List;
import java.util.Optional;

public interface FormationService {

    Formation save(Formation formation);
    Optional<Formation> findById(Long id);
    List<Formation> findAll();
    void deleteById(Long id);
    Formation update(Long id, Formation formation);
}
