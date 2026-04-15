package tn.esprit.forme.certificationservice.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.forme.certificationservice.application.dto.oralsession.CreateOralSessionRequest;
import tn.esprit.forme.certificationservice.application.dto.oralsession.OralSessionResponse;
import tn.esprit.forme.certificationservice.application.dto.oralsession.UpdateOralSessionRequest;
import tn.esprit.forme.certificationservice.application.service.OralSessionService;

import java.util.List;

@RestController
@RequestMapping("/api/oral-sessions")
@RequiredArgsConstructor

public class OralSessionController {

    private final OralSessionService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public OralSessionResponse create(@Valid @RequestBody CreateOralSessionRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<OralSessionResponse> getAll() {
        try {
            return service.findAll();
        } catch (Exception e) {
            return List.of();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public OralSessionResponse update(@PathVariable Long id, @Valid @RequestBody UpdateOralSessionRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
