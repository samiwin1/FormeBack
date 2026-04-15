package tn.esprit.forme.certificationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.forme.certificationservice.application.dto.certification.*;
import tn.esprit.forme.certificationservice.application.mapper.CertificationMapper;
import tn.esprit.forme.certificationservice.domain.entity.CertificationCatalog;
import tn.esprit.forme.certificationservice.domain.enums.CertificationStatus;
import tn.esprit.forme.certificationservice.domain.repository.CertificationCatalogRepository;
import tn.esprit.forme.certificationservice.exception.BusinessException;
import tn.esprit.forme.certificationservice.exception.NotFoundException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor

public class CertificationCatalogService {

    private final CertificationCatalogRepository repository;
    private final CertificationMapper mapper;

    @Transactional
    public CertificationResponse create(CreateCertificationRequest request) {
        validateWeights(request.weightWritten(), request.weightOral());
        CertificationCatalog entity = mapper.fromCreate(request);
        return mapper.toResponse(repository.save(entity));
    }

    public List<CertificationResponse> findAll() {
        try {
            return repository.findAll().stream()
                    .map(mapper::toResponse)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public long count() {
        return repository.count();
    }

    @Transactional
    public CertificationResponse update(Long id, UpdateCertificationRequest request) {
        validateWeights(request.weightWritten(), request.weightOral());
        CertificationCatalog entity = findEntity(id);
        mapper.applyUpdate(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional
    public CertificationResponse publish(Long id) {
        CertificationCatalog entity = findEntity(id);
        entity.setStatus(CertificationStatus.PUBLISHED);
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional
    public CertificationResponse archive(Long id) {
        CertificationCatalog entity = findEntity(id);
        entity.setStatus(CertificationStatus.ARCHIVED);
        return mapper.toResponse(repository.save(entity));
    }

    public CertificationCatalog findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Certification not found: " + id));
    }

    private void validateWeights(double weightWritten, double weightOral) {
        double sum = weightWritten + weightOral;
        if (Math.abs(sum - 1.0d) > 0.0001d) {
            throw new BusinessException("weightWritten + weightOral must equal 1.0");
        }
    }
}
