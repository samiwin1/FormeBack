package tn.esprit.forme.certificationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.forme.certificationservice.application.dto.oralsession.*;
import tn.esprit.forme.certificationservice.application.mapper.OralSessionMapper;
import tn.esprit.forme.certificationservice.domain.entity.OralSession;
import tn.esprit.forme.certificationservice.domain.repository.OralExamAssignmentRepository;
import tn.esprit.forme.certificationservice.domain.repository.OralSessionRepository;
import tn.esprit.forme.certificationservice.exception.NotFoundException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor

public class OralSessionService {

    private final OralSessionRepository repository;
    private final OralSessionMapper mapper;
    private final CertificationCatalogService certificationCatalogService;
    private final OralExamAssignmentRepository assignmentRepository;
    private final UserDirectoryAggregationService userDirectoryAggregationService;

    @Transactional
    public OralSessionResponse create(CreateOralSessionRequest request) {
        var certification = certificationCatalogService.findEntity(request.certificationId());
        OralSession entity = mapper.fromCreate(request, certification);
        OralSession saved = repository.save(entity);
        Map<Long, String> evaluatorNames = userDirectoryAggregationService.resolveDisplayNames(List.of(saved.getEvaluatorId()));
        return toResponse(saved, evaluatorNames);
    }

    @Transactional(readOnly = true)
    public List<OralSessionResponse> findAll() {
        try {
            List<OralSession> sessions = repository.findAll();
            List<Long> evaluatorIds = sessions.stream()
                    .map(OralSession::getEvaluatorId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList();
            Map<Long, String> evaluatorNames = evaluatorIds.isEmpty() ? Map.of() : userDirectoryAggregationService.resolveDisplayNames(evaluatorIds);
            return sessions.stream().map(s -> toResponse(s, evaluatorNames)).toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    @Transactional
    public OralSessionResponse update(Long id, UpdateOralSessionRequest request) {
        OralSession entity = findEntity(id);
        mapper.applyUpdate(entity, request);
        OralSession saved = repository.save(entity);
        Map<Long, String> evaluatorNames = userDirectoryAggregationService.resolveDisplayNames(List.of(saved.getEvaluatorId()));
        return toResponse(saved, evaluatorNames);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Oral session not found: " + id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public OralSession findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Oral session not found: " + id));
    }

    private OralSessionResponse toResponse(OralSession entity, Map<Long, String> evaluatorNames) {
        Long evaluatorId = entity.getEvaluatorId();
        String evaluatorName = evaluatorNames.getOrDefault(evaluatorId, "User #" + evaluatorId);
        Integer learnerCount = Math.toIntExact(assignmentRepository.countByOralSessionId(entity.getId()));
        return new OralSessionResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getCertification().getId(),
                entity.getCertification().getTitle(),
                entity.getScheduledAt(),
                entity.getDurationMinutes(),
                entity.getMeetingProvider(),
                entity.getMeetingLink(),
                evaluatorId,
                evaluatorName,
                entity.getStatus(),
                learnerCount
        );
    }
}
