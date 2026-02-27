package tn.esprit.formation_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.formation_service.entity.ResultEvaluation;
import tn.esprit.formation_service.repository.ResultEvaluationRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ResultEvaluationServiceImpl implements ResultEvaluationService {

    private final ResultEvaluationRepository resultEvaluationRepository;

    public ResultEvaluationServiceImpl(ResultEvaluationRepository resultEvaluationRepository) {
        this.resultEvaluationRepository = resultEvaluationRepository;
    }

    @Override
    @Transactional
    public ResultEvaluation save(ResultEvaluation resultEvaluation) {
        return resultEvaluationRepository.save(resultEvaluation);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResultEvaluation> findById(Long id) {
        return resultEvaluationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultEvaluation> findAll() {
        return resultEvaluationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultEvaluation> findByEvaluationId(Long evaluationId) {
        return resultEvaluationRepository.findByEvaluation_Id(evaluationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultEvaluation> findByUser_id(Long user_id) {
        return resultEvaluationRepository.findByUser_id(user_id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultEvaluation> findByEvaluationIdAndUser_id(Long evaluationId, Long userId) {
        return resultEvaluationRepository.findByEvaluationIdAndUser_id(evaluationId, userId);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        resultEvaluationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ResultEvaluation update(Long id, ResultEvaluation resultEvaluation) {
        ResultEvaluation existing = resultEvaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ResultEvaluation not found with id: " + id));
        resultEvaluation.setId(existing.getId());
        return resultEvaluationRepository.save(resultEvaluation);
    }

    @Override
    @Transactional
    public void deleteByUserIdAndFormationId(Long userId, Long formationId) {
        resultEvaluationRepository.deleteByUserIdAndFormationId(userId, formationId);
    }
}
