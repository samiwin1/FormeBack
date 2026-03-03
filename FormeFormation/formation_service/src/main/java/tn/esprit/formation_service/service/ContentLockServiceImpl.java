package tn.esprit.formation_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.formation_service.entity.ContenuFormation;
import tn.esprit.formation_service.entity.ResultEvaluation;
import tn.esprit.formation_service.repository.ContenuFormationRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ContentLockServiceImpl implements ContentLockService {

    private final ContenuFormationRepository contenuFormationRepository;
    private final ResultEvaluationService resultEvaluationService;

    public ContentLockServiceImpl(ContenuFormationRepository contenuFormationRepository,
                                   ResultEvaluationService resultEvaluationService) {
        this.contenuFormationRepository = contenuFormationRepository;
        this.resultEvaluationService = resultEvaluationService;
    }

    @Override
    @Transactional
    public void unlockNextContent(Long evaluationId, Long formationId) {
        Optional<ContenuFormation> blockWithEval = contenuFormationRepository.findByFormation_IdAndEvaluation_Id(formationId, evaluationId);
        if (blockWithEval.isEmpty()) {
            return;
        }
        Integer currentOrder = blockWithEval.get().getOrder_index();
        if (currentOrder == null) {
            return;
        }
        Optional<ContenuFormation> nextBlock = contenuFormationRepository.findByFormation_IdAndOrder_index(formationId, currentOrder + 1);
        if (nextBlock.isPresent()) {
            ContenuFormation next = nextBlock.get();
            next.setIs_locked(false);
            contenuFormationRepository.save(next);
        }
    }

    @Override
    @Transactional
    public void rollbackContent(Long evaluationId, Long formationId) {
        Optional<ContenuFormation> blockWithEval = contenuFormationRepository.findByFormation_IdAndEvaluation_Id(formationId, evaluationId);
        if (blockWithEval.isEmpty()) {
            return;
        }
        Integer currentOrder = blockWithEval.get().getOrder_index();
        if (currentOrder == null) {
            return;
        }
        List<ContenuFormation> toLock = contenuFormationRepository.findByFormation_IdAndOrder_indexGreaterThan(formationId, currentOrder);
        for (ContenuFormation block : toLock) {
            block.setIs_locked(true);
            contenuFormationRepository.save(block);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateContentAccess(Long contenuId, Long userId) {
        Optional<ContenuFormation> blockOpt = contenuFormationRepository.findById(contenuId);
        if (blockOpt.isEmpty()) {
            return false;
        }
        ContenuFormation block = blockOpt.get();
        if (Boolean.TRUE.equals(block.getIs_locked())) {
            return false;
        }
        if (block.getEvaluation() == null) {
            return true;
        }
        List<ResultEvaluation> userResults = resultEvaluationService.findByEvaluationIdAndUser_id(block.getEvaluation().getId(), userId);
        return userResults.stream().anyMatch(r -> Boolean.TRUE.equals(r.getPassed()));
    }

    @Override
    @Transactional
    public void resetLocksForFormation(Long formationId) {
        List<ContenuFormation> blocks = contenuFormationRepository.findByFormationIdOrderByOrder_indexAsc(formationId);
        if (blocks.isEmpty()) {
            return;
        }
        for (int i = 0; i < blocks.size(); i++) {
            ContenuFormation block = blocks.get(i);
            block.setIs_locked(i > 0);
            contenuFormationRepository.save(block);
        }
    }
}
