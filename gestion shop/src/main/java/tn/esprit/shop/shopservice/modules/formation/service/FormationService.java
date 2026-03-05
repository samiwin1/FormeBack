package tn.esprit.shop.shopservice.modules.formation.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.shop.shopservice.modules.formation.entity.Formation;
import tn.esprit.shop.shopservice.modules.formation.entity.FormationStatus;
import tn.esprit.shop.shopservice.modules.formation.repository.FormationRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class FormationService implements IFormationService {

    FormationRepository formationRepository;

    @Override
    public List<Formation> getAllFormations() {
        return formationRepository.findAll();
    }

    @Override
    public Formation addFormation(Formation formation) {
        return formationRepository.save(formation);
    }

    @Override
    public Formation getFormationBy(long id) {
        return formationRepository.findById(id).orElse(null);
    }

    @Override
    public Formation updateFormation(Formation formation) {
        return formationRepository.save(formation);
    }

    @Override
    public void deleteFormation(long id) {
        formationRepository.deleteById(id);
    }

    @Override
    public List<Formation> findByStatus(FormationStatus status) {
        return formationRepository.findByStatus(status);
    }

    @Override
    public List<Formation> findByCreatedBy(Long createdBy) {
        return formationRepository.findByCreatedBy(createdBy);
    }

    @Override
    public List<Formation> findByCategory(String category) {
        return formationRepository.findByCategory(category);
    }
}
