package tn.esprit.formation_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.formation_service.entity.ContenuFormation;
import tn.esprit.formation_service.entity.Evaluation;
import tn.esprit.formation_service.entity.Formation;
import tn.esprit.formation_service.repository.ContenuFormationRepository;
import tn.esprit.formation_service.repository.EvaluationRepository;
import tn.esprit.formation_service.repository.FormationRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ContenuFormationServiceImpl implements ContenuFormationService {

    private final ContenuFormationRepository contenuFormationRepository;
    private final FormationRepository formationRepository;
    private final EvaluationRepository evaluationRepository;

    public ContenuFormationServiceImpl(ContenuFormationRepository contenuFormationRepository,
                                       FormationRepository formationRepository,
                                       EvaluationRepository evaluationRepository) {
        this.contenuFormationRepository = contenuFormationRepository;
        this.formationRepository = formationRepository;
        this.evaluationRepository = evaluationRepository;
    }

    @Override
    @Transactional
    public ContenuFormation save(ContenuFormation contenuFormation) {
        if (contenuFormation.getFormation() != null && contenuFormation.getFormation().getId() != null) {
            Formation formationRef = formationRepository.getReferenceById(contenuFormation.getFormation().getId());
            contenuFormation.setFormation(formationRef);
        }
        if (contenuFormation.getEvaluation() != null && contenuFormation.getEvaluation().getId() != null) {
            Evaluation evaluationRef = evaluationRepository.getReferenceById(contenuFormation.getEvaluation().getId());
            contenuFormation.setEvaluation(evaluationRef);
        }
        return contenuFormationRepository.save(contenuFormation);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContenuFormation> findById(Long id) {
        return contenuFormationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContenuFormation> findAll() {
        return contenuFormationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContenuFormation> findByFormationId(Long formationId) {
        return contenuFormationRepository.findByFormationIdOrderByOrder_indexAsc(formationId);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        contenuFormationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ContenuFormation update(Long id, ContenuFormation contenuFormation) {
        ContenuFormation existing = contenuFormationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ContenuFormation not found with id: " + id));
        if (contenuFormation.getFormation() != null && contenuFormation.getFormation().getId() != null) {
            existing.setFormation(formationRepository.getReferenceById(contenuFormation.getFormation().getId()));
        }
        if (contenuFormation.getEvaluation() != null && contenuFormation.getEvaluation().getId() != null) {
            existing.setEvaluation(evaluationRepository.getReferenceById(contenuFormation.getEvaluation().getId()));
        }
        if (contenuFormation.getTitle() != null) existing.setTitle(contenuFormation.getTitle());
        if (contenuFormation.getContent_type() != null) existing.setContent_type(contenuFormation.getContent_type());
        if (contenuFormation.getContent_body() != null) existing.setContent_body(contenuFormation.getContent_body());
        if (contenuFormation.getOrder_index() != null) existing.setOrder_index(contenuFormation.getOrder_index());
        if (contenuFormation.getIs_locked() != null) existing.setIs_locked(contenuFormation.getIs_locked());
        return contenuFormationRepository.save(existing);
    }
}
