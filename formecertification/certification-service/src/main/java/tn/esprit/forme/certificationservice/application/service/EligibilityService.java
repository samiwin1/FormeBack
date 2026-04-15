package tn.esprit.forme.certificationservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.forme.certificationservice.exception.BusinessException;
import tn.esprit.forme.certificationservice.infrastructure.feign.FormationClient;
import tn.esprit.forme.certificationservice.infrastructure.feign.dto.ResultExamenDto;
import tn.esprit.forme.certificationservice.infrastructure.feign.dto.WrittenExamResultDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j

public class EligibilityService {

    private final FormationClient formationClient;

    public WrittenExamResultDto requireWrittenExamPassed(Long learnerId, Long formationId) {
        WrittenExamResultDto result = fetchWrittenResult(learnerId, formationId);
        if (result == null) {
            throw new BusinessException("Written exam result is unavailable (formation-service unreachable or no result)");
        }
        if (Boolean.FALSE.equals(result.getPassed())) {
            throw new BusinessException("Learner is not eligible for oral exam assignment");
        }
        return result;
    }

    public WrittenExamResultDto getWrittenExamResult(Long learnerId, Long formationId) {
        return fetchWrittenResult(learnerId, formationId);
    }

    public List<WrittenExamResultDto> getPassedLearnersForFormation(Long formationId) {
        var exam = safeGetExamByFormationId(formationId);
        if (exam == null || exam.getId() == null) {
            return List.of();
        }

        List<ResultExamenDto> rawResults = safeGetResultExamensByExamenId(exam.getId());
        if (rawResults == null || rawResults.isEmpty()) {
            return List.of();
        }

        Map<Long, ResultExamenDto> bestByLearner = rawResults.stream()
                .filter(Objects::nonNull)
                .filter(r -> r.getUserId() != null)
                .collect(Collectors.toMap(
                        ResultExamenDto::getUserId,
                        r -> r,
                        (a, b) -> scoreOrZero(a) >= scoreOrZero(b) ? a : b
                ));

        List<WrittenExamResultDto> passed = new ArrayList<>();
        for (ResultExamenDto result : bestByLearner.values()) {
            if (!Boolean.TRUE.equals(result.getPassed())) {
                continue;
            }
            WrittenExamResultDto dto = new WrittenExamResultDto();
            dto.setLearnerId(result.getUserId());
            dto.setFormationId(formationId);
            dto.setScore(result.getScore() == null ? null : result.getScore().doubleValue());
            dto.setPassed(true);
            passed.add(dto);
        }
        return passed;
    }

    private WrittenExamResultDto fetchWrittenResult(Long learnerId, Long formationId) {
        var history = safeGetExamHistory(learnerId, formationId);
        if (history == null || history.isEmpty()) {
            return null;
        }

        var latest = history.stream()
                .max(Comparator.comparing(item -> item.getSubmittedAt(), Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElse(null);
        if (latest == null) {
            return null;
        }

        WrittenExamResultDto dto = new WrittenExamResultDto();
        dto.setLearnerId(learnerId);
        dto.setFormationId(formationId);
        dto.setScore(latest.getScore() == null ? null : latest.getScore().doubleValue());
        dto.setPassed(Boolean.TRUE.equals(latest.getPassed()));
        return dto;
    }

    private int scoreOrZero(ResultExamenDto dto) {
        return dto.getScore() == null ? 0 : dto.getScore();
    }

    private tn.esprit.forme.certificationservice.infrastructure.feign.dto.ExamDto safeGetExamByFormationId(Long formationId) {
        try {
            return formationClient.getExamByFormationId(formationId);
        } catch (Exception ex) {
            log.warn("formation-service unavailable while loading exam for formationId={}: {}", formationId, ex.getMessage());
            return null;
        }
    }

    private List<ResultExamenDto> safeGetResultExamensByExamenId(Long examenId) {
        try {
            return formationClient.getResultExamensByExamenId(examenId);
        } catch (Exception ex) {
            log.warn("formation-service unavailable while loading exam results for examenId={}: {}", examenId, ex.getMessage());
            return List.of();
        }
    }

    private List<tn.esprit.forme.certificationservice.infrastructure.feign.dto.ExamHistoryItemDto> safeGetExamHistory(Long learnerId, Long formationId) {
        try {
            return formationClient.getExamHistory(learnerId, formationId);
        } catch (Exception ex) {
            log.warn("formation-service unavailable while loading exam history for learnerId={}, formationId={}: {}",
                    learnerId, formationId, ex.getMessage());
            return List.of();
        }
    }
}
