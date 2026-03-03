package tn.esprit.formation_service.service;

import tn.esprit.formation_service.entity.ResultExamen;

import java.util.Map;

public interface ExamEngineService {

    /**
     * Starts exam for user. Creates ResultExamen with start_time=NOW, end_time=null.
     * Throws if not eligible or active exam already exists.
     */
    ResultExamen startExam(Long examenId, Long userId);

    /**
     * Saves answers for in-progress exam. Rejects if exam ended.
     */
    void saveAnswer(Long resultExamenId, Map<String, Integer> answers);

    /**
     * Submits exam: computes score, sets end_time, passed. Rejects if already ended.
     */
    ResultExamen submitExam(Long resultExamenId);

    /**
     * Returns remaining seconds. Returns 0 if exam ended.
     */
    int getRemainingSeconds(Long resultExamenId);
}
