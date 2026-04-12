package tn.esprit.forme.certificationservice.application.dto.linkedin;

public record LinkedInPostResponse(
        Long issuedCertificationId,
        String generatedPost,
        String certificationTitle,
        String linkedInShareUrl
) {
}

