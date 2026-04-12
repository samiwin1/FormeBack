package tn.esprit.formation_service.service;

import tn.esprit.formation_service.entity.ResultExamen;

import java.util.List;
import java.util.Optional;

public interface ResultExamenService {

    ResultExamen save(ResultExamen resultExamen);
    Optional<ResultExamen> findById(Long id);
    List<ResultExamen> findAll();
    List<ResultExamen> findByExamenId(Long examenId);
    List<ResultExamen> findByUser_id(Long user_id);
    void deleteById(Long id);
    ResultExamen update(Long id, ResultExamen resultExamen);
    void deleteByUserIdAndFormationId(Long userId, Long formationId);
}
