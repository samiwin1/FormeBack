package tn.esprit.formation_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.formation_service.entity.ResultExamen;
import tn.esprit.formation_service.repository.ResultExamenRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ResultExamenServiceImpl implements ResultExamenService {

    private final ResultExamenRepository resultExamenRepository;

    public ResultExamenServiceImpl(ResultExamenRepository resultExamenRepository) {
        this.resultExamenRepository = resultExamenRepository;
    }

    @Override
    @Transactional
    public ResultExamen save(ResultExamen resultExamen) {
        return resultExamenRepository.save(resultExamen);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResultExamen> findById(Long id) {
        return resultExamenRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultExamen> findAll() {
        return resultExamenRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultExamen> findByExamenId(Long examenId) {
        return resultExamenRepository.findByExamen_Id(examenId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultExamen> findByUser_id(Long user_id) {
        return resultExamenRepository.findByUser_id(user_id);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        resultExamenRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ResultExamen update(Long id, ResultExamen resultExamen) {
        ResultExamen existing = resultExamenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ResultExamen not found with id: " + id));
        resultExamen.setId(existing.getId());
        return resultExamenRepository.save(resultExamen);
    }

    @Override
    @Transactional
    public void deleteByUserIdAndFormationId(Long userId, Long formationId) {
        resultExamenRepository.deleteByUser_idAndExamen_Formation_Id(userId, formationId);
    }
}
