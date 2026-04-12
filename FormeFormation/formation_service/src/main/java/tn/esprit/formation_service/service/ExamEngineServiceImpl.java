package tn.esprit.formation_service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.formation_service.entity.Examen;
import tn.esprit.formation_service.entity.ResultExamen;
import tn.esprit.formation_service.repository.ExamenRepository;
import tn.esprit.formation_service.repository.ResultExamenRepository;
import tn.esprit.formation_service.util.QuizScoringUtil;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExamEngineServiceImpl implements ExamEngineService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ResultExamenRepository resultExamenRepository;
    private final ExamenRepository examenRepository;
    private final ValidationService validationService;

    public ExamEngineServiceImpl(ResultExamenRepository resultExamenRepository,
                                 ExamenRepository examenRepository,
                                 ValidationService validationService) {
        this.resultExamenRepository = resultExamenRepository;
        this.examenRepository = examenRepository;
        this.validationService = validationService;
    }

    @Override
    @Transactional
    public ResultExamen startExam(Long examenId, Long userId) {
        Examen examen = examenRepository.findById(examenId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found"));

        Long formationId = examen.getFormation() != null ? examen.getFormation().getId() : null;
        if (formationId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exam has no formation");
        }

        if (!validationService.canStartExam(userId, formationId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not eligible to start exam");
        }

        if (resultExamenRepository.findByExamen_IdAndUser_idAndEnd_timeIsNull(examenId, userId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exam already in progress");
        }

        ResultExamen result = new ResultExamen();
        result.setExamen(examen);
        result.setUser_id(userId);
        result.setStart_time(Instant.now());
        result.setEnd_time(null);
        result.setSubmitted_answers("{}");
        result.setScore(null);
        result.setPassed(null);

        return resultExamenRepository.save(result);
    }

    @Override
    @Transactional
    public void saveAnswer(Long resultExamenId, Map<String, Integer> answers) {
        ResultExamen result = resultExamenRepository.findById(resultExamenId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Result not found"));

        if (result.getEnd_time() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exam already ended");
        }

        if (autoSubmitIfExpired(result)) {
            return;
        }

        try {
            String json = answers != null && !answers.isEmpty()
                    ? OBJECT_MAPPER.writeValueAsString(answers)
                    : "{}";
            result.setSubmitted_answers(json);
            resultExamenRepository.save(result);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid answers format");
        }
    }

    @Override
    @Transactional
    public ResultExamen submitExam(Long resultExamenId) {
        ResultExamen result = resultExamenRepository.findById(resultExamenId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Result not found"));

        if (result.getEnd_time() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exam already ended");
        }

        if (autoSubmitIfExpired(result)) {
            return resultExamenRepository.findById(resultExamenId).orElse(result);
        }

        Examen examen = result.getExamen();
        if (examen == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exam not found");
        }

        Map<String, Integer> answers = parseAnswers(result.getSubmitted_answers());
        int score = QuizScoringUtil.scoreStructuredAnswers(examen.getContent(), answers);
        int passingScore = examen.getPassing_score() != null ? examen.getPassing_score() : 75;
        boolean passed = score >= passingScore;

        result.setScore(score);
        result.setPassed(passed);
        result.setEnd_time(Instant.now());

        return resultExamenRepository.save(result);
    }

    @Override
    @Transactional(readOnly = true)
    public int getRemainingSeconds(Long resultExamenId) {
        ResultExamen result = resultExamenRepository.findById(resultExamenId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Result not found"));

        if (result.getEnd_time() != null) {
            return 0;
        }

        Examen examen = result.getExamen();
        if (examen == null || examen.getDuration_minutes() == null) {
            return 0;
        }

        long elapsedSeconds = Instant.now().getEpochSecond() - result.getStart_time().getEpochSecond();
        long totalSeconds = examen.getDuration_minutes() * 60L;
        long remaining = totalSeconds - elapsedSeconds;

        return (int) Math.max(0, remaining);
    }

    private boolean autoSubmitIfExpired(ResultExamen result) {
        Examen examen = result.getExamen();
        if (examen == null || examen.getDuration_minutes() == null || result.getStart_time() == null) {
            return false;
        }

        long elapsedMinutes = (Instant.now().getEpochSecond() - result.getStart_time().getEpochSecond()) / 60;
        if (elapsedMinutes < examen.getDuration_minutes()) {
            return false;
        }

        Map<String, Integer> answers = parseAnswers(result.getSubmitted_answers());
        int score = QuizScoringUtil.scoreStructuredAnswers(examen.getContent(), answers);
        int passingScore = examen.getPassing_score() != null ? examen.getPassing_score() : 75;
        boolean passed = score >= passingScore;

        result.setScore(score);
        result.setPassed(passed);
        result.setEnd_time(Instant.now());
        resultExamenRepository.save(result);

        return true;
    }

    private Map<String, Integer> parseAnswers(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
