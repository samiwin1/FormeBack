package tn.esprit.formation_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.formation_service.dto.ExamHistoryItem;
import tn.esprit.formation_service.entity.Examen;
import tn.esprit.formation_service.entity.Formation;
import tn.esprit.formation_service.entity.ResultExamen;
import tn.esprit.formation_service.repository.ExamenRepository;
import tn.esprit.formation_service.repository.FormationRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ExamenServiceImpl implements ExamenService {

    private final ExamenRepository examenRepository;
    private final FormationRepository formationRepository;
    private final ResultExamenService resultExamenService;

    public ExamenServiceImpl(ExamenRepository examenRepository,
                             FormationRepository formationRepository,
                             ResultExamenService resultExamenService) {
        this.examenRepository = examenRepository;
        this.formationRepository = formationRepository;
        this.resultExamenService = resultExamenService;
    }

    @Override
    @Transactional
    public Examen save(Examen examen) {
        if (examen.getFormation() != null && examen.getFormation().getId() != null) {
            Formation formationRef = formationRepository.getReferenceById(examen.getFormation().getId());
            examen.setFormation(formationRef);
        }
        return examenRepository.save(examen);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Examen> findById(Long id) {
        return examenRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Examen> findByFormationId(Long formationId) {
        return examenRepository.findByFormation_Id(formationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Examen> findAll() {
        return examenRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        examenRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Examen update(Long id, Examen examen) {
        Examen existing = examenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen not found with id: " + id));
        examen.setId(existing.getId());
        return examenRepository.save(examen);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamHistoryItem> getExamHistory(Long userId, Long formationId) {
        List<ResultExamen> results = resultExamenService.findByUser_id(userId);
        List<ExamHistoryItem> items = new ArrayList<>();
        for (ResultExamen r : results) {
            Examen examen = r.getExamen();
            if (examen == null) continue;
            if (formationId != null && examen.getFormation() != null
                    && !formationId.equals(examen.getFormation().getId())) {
                continue;
            }
            ExamHistoryItem item = new ExamHistoryItem();
            item.setExamenId(examen.getId());
            item.setExamTitle(examen.getTitle());
            item.setFormationId(examen.getFormation() != null ? examen.getFormation().getId() : null);
            item.setScore(r.getScore());
            item.setPassed(r.getPassed());
            if (r.getStart_time() != null && r.getEnd_time() != null) {
                item.setDurationMinutes(Duration.between(r.getStart_time(), r.getEnd_time()).toMinutes());
            }
            item.setSubmittedAt(r.getEnd_time() != null ? r.getEnd_time() : r.getStart_time());
            items.add(item);
        }
        items.sort(Comparator.comparing(ExamHistoryItem::getSubmittedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return items;
    }
}
