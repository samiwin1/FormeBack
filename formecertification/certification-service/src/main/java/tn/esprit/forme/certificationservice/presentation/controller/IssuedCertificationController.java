package tn.esprit.forme.certificationservice.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tn.esprit.forme.certificationservice.application.dto.issued.IssuedCertificationResponse;
import tn.esprit.forme.certificationservice.application.service.CertificateEventsService;
import tn.esprit.forme.certificationservice.application.service.IssuedCertificationQueryService;
import tn.esprit.forme.certificationservice.domain.enums.IssuedCertificationStatus;
import tn.esprit.forme.certificationservice.security.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor

public class IssuedCertificationController {

    private final IssuedCertificationQueryService service;
    private final CertificateEventsService certificateEventsService;

    @GetMapping("/me/certifications")
    @PreAuthorize("hasRole('USER') and @professionGuard.isLearner()")
    public List<IssuedCertificationResponse> myCertifications() {
        return service.findForLearner(SecurityUtils.currentUserId());
    }

    @GetMapping("/me/issued-certifications")
    @PreAuthorize("hasRole('USER') and @professionGuard.isLearner()")
    public List<IssuedCertificationResponse> myIssuedCertifications() {
        return service.findForLearner(SecurityUtils.currentUserId());
    }

    @GetMapping("/me/certifications/{id}/pdf")
    @PreAuthorize("hasRole('USER') and @professionGuard.isLearner()")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Resource resource = service.loadPdf(SecurityUtils.currentUserId(), id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate-" + id + ".pdf")
                .body(resource);
    }

    @GetMapping(value = "/me/certifications/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('USER') and @professionGuard.isLearner()")
    public SseEmitter certificateEvents() {
        Long learnerId = SecurityUtils.currentUserId();
        try {
            return certificateEventsService.subscribe(learnerId);
        } catch (Exception ex) {
            // Fallback to a simple keep-alive stream instead of 500
            SseEmitter emitter = new SseEmitter(0L);
            try {
                emitter.send(SseEmitter.event()
                        .name("keep-alive")
                        .data("connected"));
            } catch (Exception ignored) {
            }
            return emitter;
        }
    }

    @GetMapping("/issued-certifications/{id}/pdf")
    @PreAuthorize("hasRole('USER') and @professionGuard.isLearner()")
    public ResponseEntity<Resource> downloadIssuedPdf(@PathVariable Long id) {
        Resource resource = service.loadPdf(SecurityUtils.currentUserId(), id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate-" + id + ".pdf")
                .body(resource);
    }

    @GetMapping("/admin/issued-certifications")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<IssuedCertificationResponse> adminList(
            @RequestParam(value = "learnerId", required = false) Long learnerId,
            @RequestParam(value = "formationId", required = false) Long formationId,
            @RequestParam(value = "status", required = false) IssuedCertificationStatus status
    ) {
        try {
            return service.findForAdmin(learnerId, formationId, status);
        } catch (Exception e) {
            return List.of();
        }
    }

    @PatchMapping("/admin/issued-certifications/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public IssuedCertificationResponse revoke(@PathVariable Long id) {
        return service.revoke(id);
    }
}
