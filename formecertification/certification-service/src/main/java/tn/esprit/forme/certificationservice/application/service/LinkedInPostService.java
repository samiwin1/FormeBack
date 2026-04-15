package tn.esprit.forme.certificationservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.forme.certificationservice.application.client.GoogleAIClient;
import tn.esprit.forme.certificationservice.application.dto.linkedin.LinkedInPostResponse;
import tn.esprit.forme.certificationservice.domain.entity.CertificationCatalog;
import tn.esprit.forme.certificationservice.domain.entity.IssuedCertification;
import tn.esprit.forme.certificationservice.domain.enums.IssuedCertificationStatus;
import tn.esprit.forme.certificationservice.domain.repository.CertificationCatalogRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j

public class LinkedInPostService {

    private final GoogleAIClient googleAIClient;
    private final IssuedCertificationQueryService issuedCertificationQueryService;
    private final CertificationCatalogRepository certificationCatalogRepository;
    private final UserDirectoryAggregationService userDirectoryAggregationService;

    public LinkedInPostResponse generatePost(Long learnerId, Long issuedCertificationId) {

        IssuedCertification issued = issuedCertificationQueryService
                .findById(issuedCertificationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Certificate not found"));

        if (!issued.getLearnerId().equals(learnerId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Not your certificate"
            );
        }

        if (issued.getStatus() != IssuedCertificationStatus.ISSUED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Certificate is not in ISSUED status"
            );
        }

        CertificationCatalog cert = certificationCatalogRepository
                .findById(issued.getCertification().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Certification not found"));

        String learnerName;
        try {
            var entry = userDirectoryAggregationService.getById(learnerId);
            learnerName = entry != null && entry.displayName() != null
                    ? entry.displayName()
                    : null;
        } catch (Exception ex) {
            learnerName = null;
        }
        if (learnerName == null || learnerName.startsWith("User #")) {
            learnerName = "A dedicated professional";
        }

        String prompt = """
                You are a professional LinkedIn post writer.
                Write an engaging, authentic LinkedIn post for someone
                who just received a professional certification.

                Details:
                - Person name: %s
                - Certification: %s
                - Domain: %s
                - Level: %s
                - Final score: %.1f/100
                - Platform: ForMe
                - Date: %s

                Requirements:
                - Tone: professional but human and warm
                - Length: 150-200 words maximum
                - Include 3-5 relevant hashtags at the end
                - Mention the hard work and the oral exam component
                - Do NOT use generic phrases like "I am pleased to announce"
                - Make it feel personal and genuine
                - Start with an attention-grabbing first line
                - Include 1-2 emojis naturally placed
                - End with hashtags including #ForMe #Certification

                Return ONLY the post text. No explanation, no preamble.
                """.formatted(
                learnerName,
                cert.getTitle(),
                cert.getDomain(),
                cert.getLevel(),
                issued.getFinalScore(),
                issued.getIssuedAt().toLocalDate().toString()
        );

        log.info("Generating LinkedIn post for learnerId={} certId={}",
                learnerId, issuedCertificationId);
        String generatedPost;
        try {
            generatedPost = googleAIClient.generateLinkedInPost(prompt);
        } catch (RuntimeException ex) {
            log.warn("AI post generation unavailable, using fallback template. Cause: {}", ex.getMessage());
            generatedPost = fallbackPost(
                    learnerName,
                    cert.getTitle(),
                    cert.getDomain(),
                    cert.getLevel(),
                    issued.getFinalScore()
            );
        }

        String verificationUrl = "https://forme.tn/verify/" + issued.getCertificateNumber();
        
        // Generate the PDF download URL
        String pdfUrl = "http://localhost:8090/api/me/certifications/" + issuedCertificationId + "/pdf";
        
        // Create LinkedIn share URL with the PDF link in the post
        String linkedInShareUrl =
                "https://www.linkedin.com/sharing/share-offsite/?url="
                        + URLEncoder.encode(pdfUrl, StandardCharsets.UTF_8);

        return new LinkedInPostResponse(
                issuedCertificationId,
                generatedPost + "\n\n📄 View my certificate: " + pdfUrl,
                cert.getTitle(),
                linkedInShareUrl
        );
    }

    private String fallbackPost(
            String learnerName,
            String certificationTitle,
            String domain,
            String level,
            double finalScore
    ) {
        String safeDomain = domain == null || domain.isBlank() ? "Technology" : domain;
        String safeLevel = level == null || level.isBlank() ? "Professional" : level;

        return """
                Big milestone unlocked for me today. 🚀

                I'm %s, and I successfully earned the %s certification in %s (%s level) on ForMe, with a final score of %.1f/100.

                This journey required consistency, deep practice, and preparation for both written and oral evaluation phases. The oral component especially pushed me to structure my thinking clearly and communicate under pressure.

                Grateful for the learning process and excited to apply these skills in real projects and team collaboration.

                #ForMe #Certification #ContinuousLearning #CareerGrowth #ProfessionalDevelopment
                """.formatted(
                learnerName,
                certificationTitle,
                safeDomain,
                safeLevel,
                finalScore
        );
    }
}

