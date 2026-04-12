package tn.esprit.formation_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.formation_service.entity.Formation;
import tn.esprit.formation_service.repository.FormationRepository;

import java.util.List;
import java.util.Optional;

@Service
public class FormationServiceImpl implements FormationService {

    private final FormationRepository formationRepository;

    public FormationServiceImpl(FormationRepository formationRepository) {
        this.formationRepository = formationRepository;
    }

    @Override
    @Transactional
    public Formation save(Formation formation) {
        return formationRepository.save(formation);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Formation> findById(Long id) {
        return formationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Formation> findAll() {
        return formationRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        formationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Formation update(Long id, Formation formation) {
        Formation existing = formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation not found with id: " + id));
        if (formation.getTitle() != null) existing.setTitle(formation.getTitle());
        if (formation.getDescription() != null) existing.setDescription(formation.getDescription());
        if (formation.getCategory() != null) existing.setCategory(formation.getCategory());
        if (formation.getLevel() != null) existing.setLevel(formation.getLevel());
        if (formation.getObjectives() != null) existing.setObjectives(formation.getObjectives());
        if (formation.getSkills_targeted() != null) existing.setSkills_targeted(formation.getSkills_targeted());
        if (formation.getStatus() != null) existing.setStatus(formation.getStatus());
        if (formation.getCreated_by() != null) existing.setCreated_by(formation.getCreated_by());
        return formationRepository.save(existing);
    }
}
