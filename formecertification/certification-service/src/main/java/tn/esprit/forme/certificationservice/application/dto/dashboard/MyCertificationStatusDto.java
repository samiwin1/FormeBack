package tn.esprit.forme.certificationservice.application.dto.dashboard;

public record MyCertificationStatusDto(
        String status,
        Long issuedCertificationId,
        String certificateNumber,
        Double finalScore,
        String pdfPath,
        Boolean downloadable
) {
}
