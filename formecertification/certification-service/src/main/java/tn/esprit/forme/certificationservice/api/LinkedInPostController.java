package tn.esprit.forme.certificationservice.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.forme.certificationservice.application.dto.linkedin.LinkedInPostRequest;
import tn.esprit.forme.certificationservice.application.dto.linkedin.LinkedInPostResponse;
import tn.esprit.forme.certificationservice.application.service.LinkedInPostService;
import tn.esprit.forme.certificationservice.security.SecurityUtils;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor

public class LinkedInPostController {

    private final LinkedInPostService linkedInPostService;

    @PostMapping("/linkedin-post")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LinkedInPostResponse> generatePost(
            @RequestBody @Valid LinkedInPostRequest request) {

        Long learnerId = SecurityUtils.currentUserId();

        LinkedInPostResponse response = linkedInPostService
                .generatePost(learnerId, request.issuedCertificationId());

        return ResponseEntity.ok(response);
    }
}

