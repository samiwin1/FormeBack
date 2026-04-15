package tn.esprit.forme.certificationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.forme.certificationservice.application.dto.issued.IssuedCertificationResponse;
import tn.esprit.forme.certificationservice.application.mapper.IssuedCertificationMapper;
import tn.esprit.forme.certificationservice.domain.entity.IssuedCertification;
import tn.esprit.forme.certificationservice.domain.enums.IssuedCertificationStatus;
import tn.esprit.forme.certificationservice.domain.repository.IssuedCertificationRepository;
import tn.esprit.forme.certificationservice.exception.NotFoundException;
import tn.esprit.forme.certificationservice.infrastructure.feign.FormationClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor

public class IssuedCertificationQueryService {

    private final IssuedCertificationRepository repository;
    private final IssuedCertificationMapper mapper;
    private final PdfGenerationService pdfGenerationService;
    private final UserDirectoryAggregationService userDirectoryAggregationService;
    private final FormationClient formationClient;
    @Value("${app.certificates.output-dir:${app.pdf.storage-path:generated-certificates}}")
    private String certificatesOutputDir;

    @Transactional(readOnly = true)
    public java.util.Optional<IssuedCertification> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<IssuedCertificationResponse> findForLearner(Long learnerId) {
        return repository.findByLearnerId(learnerId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<IssuedCertificationResponse> findForAdmin(Long learnerId, Long formationId, IssuedCertificationStatus status) {
        try {
            List<IssuedCertification> rows;
            if (learnerId != null && formationId != null && status != null) {
                rows = repository.findByLearnerIdAndFormationIdAndStatus(learnerId, formationId, status);
            } else if (learnerId != null && status != null) {
                rows = repository.findByLearnerIdAndStatus(learnerId, status);
            } else if (formationId != null && status != null) {
                rows = repository.findByFormationIdAndStatus(formationId, status);
            } else if (status != null) {
                rows = repository.findByStatus(status);
            } else {
                rows = repository.findAll();
                if (learnerId != null) {
                    rows = rows.stream().filter(r -> learnerId.equals(r.getLearnerId())).toList();
                }
                if (formationId != null) {
                    rows = rows.stream().filter(r -> formationId.equals(r.getFormationId())).toList();
                }
            }
            return rows.stream().map(mapper::toResponse).toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public Resource loadPdf(Long learnerId, Long certificationId) {
        var certificate = repository.findById(certificationId)
                .orElseThrow(() -> new NotFoundException("Issued certification not found: " + certificationId));

        if (!certificate.getLearnerId().equals(learnerId)) {
            throw new NotFoundException("Issued certification not found for learner");
        }

        Path path = resolveOrRegeneratePdf(certificate);

        return new FileSystemResource(path);
    }

    @Transactional
    public IssuedCertificationResponse revoke(Long issuedCertificationId) {
        IssuedCertification certificate = repository.findById(issuedCertificationId)
                .orElseThrow(() -> new NotFoundException("Issued certification not found: " + issuedCertificationId));
        certificate.setStatus(IssuedCertificationStatus.REVOKED);
        return mapper.toResponse(repository.save(certificate));
    }

    private Path resolvePdfPath(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            throw new NotFoundException("PDF file path is missing");
        }

        String normalizedStoredPath = storedPath.trim().replace("\\", "/");
        Path directPath = Path.of(normalizedStoredPath);
        if (directPath.isAbsolute()) {
            return directPath.normalize();
        }

        Path outputDir = Path.of(certificatesOutputDir).toAbsolutePath().normalize();

        String outputDirName = outputDir.getFileName() == null ? "" : outputDir.getFileName().toString();
        String stripped = normalizedStoredPath;
        if (!outputDirName.isBlank()) {
            String prefix = outputDirName + "/";
            if (stripped.startsWith(prefix)) {
                stripped = stripped.substring(prefix.length());
            }
        }

        Path candidate1 = outputDir.resolve(stripped).normalize();
        
        // Security check: Ensure resolved path is within output directory
        if (!candidate1.startsWith(outputDir)) {
            throw new SecurityException("Invalid PDF path: path traversal attempt detected");
        }
        
        if (Files.exists(candidate1)) {
            return candidate1;
        }

        Path candidate2 = Path.of(normalizedStoredPath).toAbsolutePath().normalize();
        if (Files.exists(candidate2)) {
            return candidate2;
        }

        Path candidate3 = outputDir.resolve(normalizedStoredPath).normalize();
        
        // Security check for candidate3
        if (!candidate3.startsWith(outputDir)) {
            throw new SecurityException("Invalid PDF path: path traversal attempt detected");
        }
        
        if (Files.exists(candidate3)) {
            return candidate3;
        }

        return candidate1;
    }

    /**
     * If the stored PDF is missing, regenerate it on the fly using PdfGenerationService
     * and update the entity's pdfPath so subsequent calls work without regeneration.
     */
    private Path resolveOrRegeneratePdf(IssuedCertification certificate) {
        try {
            Path existing = resolvePdfPath(certificate.getPdfPath());
            if (Files.exists(existing)) {
                return existing;
            }
        } catch (NotFoundException ignored) {
            // fall through to regeneration
        }

        String learnerName;
        try {
            var entry = userDirectoryAggregationService.getById(certificate.getLearnerId());
            learnerName = entry != null && entry.displayName() != null
                    ? entry.displayName()
                    : "Learner #" + certificate.getLearnerId();
        } catch (Exception ex) {
            learnerName = "Learner #" + certificate.getLearnerId();
        }

        String formationName;
        try {
            var formation = formationClient.getFormationById(certificate.getFormationId());
            formationName = formation != null && formation.getTitle() != null
                    ? formation.getTitle()
                    : "Formation #" + certificate.getFormationId();
        } catch (Exception ex) {
            formationName = "Formation #" + certificate.getFormationId();
        }

        String fileName = pdfGenerationService.generateCertificatePdf(
                learnerName,
                formationName,
                certificate.getCertification().getTitle(),
                certificate.getFinalScore(),
                certificate.getIssuedAt(),
                certificate.getCertificateNumber()
        );

        certificate.setPdfPath(fileName);
        repository.save(certificate);

        return resolvePdfPath(fileName);
    }

}
