package tn.esprit.forme.certificationservice.application.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service

public class PdfGenerationService {

    private final Path outputDirectory;

    public PdfGenerationService(@Value("${app.certificates.output-dir:${app.pdf.storage-path:generated-certificates}}") String storagePath) {
        this.outputDirectory = Path.of(storagePath);
    }

    public String generateCertificatePdf(String fullName,
                                         String formationName,
                                         String certificationName,
                                         double finalScore,
                                         LocalDateTime issuedAt,
                                         String certificateNumber) {
        try {
            Files.createDirectories(outputDirectory);
            String fileName = "certificate-" + UUID.randomUUID() + ".pdf";
            Path outputPath = outputDirectory.toAbsolutePath().resolve(fileName);

            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputPath.toFile()));
            document.open();

            // Add decorative border
            addDecorativeBorder(document, writer);

            // Add spacing from top
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            addLogoIfExists(document);

            // Header with decorative line
            Font certificateTitleFont = new Font(Font.HELVETICA, 38, Font.BOLD, new Color(16, 185, 129));
            Paragraph certificateTitle = new Paragraph("CERTIFICATE", certificateTitleFont);
            certificateTitle.setAlignment(Element.ALIGN_CENTER);
            certificateTitle.setSpacingBefore(15f);
            certificateTitle.setSpacingAfter(5f);
            document.add(certificateTitle);

            // Decorative line under title
            addDecorativeLine(document, new Color(16, 185, 129));

            Font ofCompletionFont = new Font(Font.HELVETICA, 14, Font.ITALIC, new Color(100, 116, 139));
            Paragraph ofCompletion = new Paragraph("OF COMPLETION", ofCompletionFont);
            ofCompletion.setAlignment(Element.ALIGN_CENTER);
            ofCompletion.setSpacingAfter(30f);
            document.add(ofCompletion);

            // Intro text
            Font bodyFont = new Font(Font.HELVETICA, 12, Font.NORMAL, new Color(71, 85, 105));
            Paragraph intro = new Paragraph("This certifies that", bodyFont);
            intro.setAlignment(Element.ALIGN_CENTER);
            intro.setSpacingAfter(20f);
            document.add(intro);

            // Learner name with decorative box
            addNameBox(document, fullName);

            // Description with better formatting
            String descriptionText = String.format(
                    "has successfully completed the \"%s\" certification for the \"%s\" formation\nwith a final score of %.2f/20.",
                    certificationName,
                    formationName,
                    finalScore
            );
            Font descFont = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(71, 85, 105));
            Paragraph description = new Paragraph(descriptionText, descFont);
            description.setAlignment(Element.ALIGN_CENTER);
            description.setSpacingAfter(25f);
            description.setLeading(16f);
            document.add(description);

            // Date with icon-like styling
            String dateText = "Issued on " + issuedAt.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            Font dateFont = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(100, 116, 139));
            Paragraph dateParagraph = new Paragraph(dateText, dateFont);
            dateParagraph.setAlignment(Element.ALIGN_CENTER);
            dateParagraph.setSpacingAfter(30f);
            document.add(dateParagraph);

            // Certificate number in a box
            addCertificateNumberBox(document, certificateNumber);

            // Signatures row with better styling
            addSignatureSection(document);

            // QR code + verification summary
            String verificationText = "Verification: " + certificateNumber + " | "
                    + issuedAt.format(DateTimeFormatter.ISO_LOCAL_DATE);

            addFooterWithQR(document, verificationText);

            document.close();
            return fileName;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate PDF", ex);
        }
    }

    private void addDecorativeBorder(Document document, PdfWriter writer) throws DocumentException {
        PdfPTable borderTable = new PdfPTable(1);
        borderTable.setWidthPercentage(100);
        borderTable.getDefaultCell().setBorder(Rectangle.BOX);
        borderTable.getDefaultCell().setBorderWidth(3f);
        borderTable.getDefaultCell().setBorderColor(new Color(16, 185, 129));
        borderTable.getDefaultCell().setPadding(0);
        
        PdfPCell innerCell = new PdfPCell();
        innerCell.setBorder(Rectangle.BOX);
        innerCell.setBorderWidth(1f);
        innerCell.setBorderColor(new Color(203, 213, 225));
        innerCell.setPadding(0);
        innerCell.setFixedHeight(document.getPageSize().getHeight() - 80);
        
        borderTable.addCell(innerCell);
    }

    private void addDecorativeLine(Document document, Color color) throws DocumentException {
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(30);
        line.setHorizontalAlignment(Element.ALIGN_CENTER);
        line.setSpacingBefore(8f);
        line.setSpacingAfter(8f);
        
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBorderWidthBottom(2f);
        cell.setBorderColorBottom(color);
        cell.setFixedHeight(5f);
        
        line.addCell(cell);
        document.add(line);
    }

    private void addNameBox(Document document, String fullName) throws DocumentException {
        PdfPTable nameTable = new PdfPTable(1);
        nameTable.setWidthPercentage(70);
        nameTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        nameTable.setSpacingBefore(10f);
        nameTable.setSpacingAfter(25f);
        
        Font nameFont = new Font(Font.HELVETICA, 28, Font.BOLD, new Color(15, 23, 42));
        Paragraph namePara = new Paragraph(fullName, nameFont);
        namePara.setAlignment(Element.ALIGN_CENTER);
        
        PdfPCell nameCell = new PdfPCell(namePara);
        nameCell.setBorder(Rectangle.NO_BORDER);
        nameCell.setBorderWidthBottom(2f);
        nameCell.setBorderColorBottom(new Color(203, 213, 225));
        nameCell.setPadding(10f);
        nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        nameTable.addCell(nameCell);
        document.add(nameTable);
    }

    private void addCertificateNumberBox(Document document, String certificateNumber) throws DocumentException {
        PdfPTable certNumTable = new PdfPTable(1);
        certNumTable.setWidthPercentage(50);
        certNumTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        certNumTable.setSpacingBefore(10f);
        certNumTable.setSpacingAfter(35f);
        
        Font certNumFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(71, 85, 105));
        Paragraph certNumPara = new Paragraph("Certificate No: " + certificateNumber, certNumFont);
        certNumPara.setAlignment(Element.ALIGN_CENTER);
        
        PdfPCell certNumCell = new PdfPCell(certNumPara);
        certNumCell.setBorder(Rectangle.BOX);
        certNumCell.setBorderWidth(1f);
        certNumCell.setBorderColor(new Color(203, 213, 225));
        certNumCell.setBackgroundColor(new Color(248, 250, 252));
        certNumCell.setPadding(8f);
        certNumCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        certNumTable.addCell(certNumCell);
        document.add(certNumTable);
    }

    private void addSignatureSection(Document document) throws DocumentException {
        PdfPTable signatures = new PdfPTable(2);
        signatures.setWidthPercentage(85);
        signatures.setHorizontalAlignment(Element.ALIGN_CENTER);
        signatures.setSpacingBefore(30f);
        signatures.setSpacingAfter(20f);
        
        Font signatureFont = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(100, 116, 139));
        Font signatureLabelFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(71, 85, 105));
        
        // Left signature
        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.setHorizontalAlignment(Element.ALIGN_CENTER);
        left.setPaddingTop(10f);
        
        Paragraph leftLine = new Paragraph("_____________________________", signatureFont);
        leftLine.setAlignment(Element.ALIGN_CENTER);
        left.addElement(leftLine);
        
        Paragraph leftLabel = new Paragraph("Training Coordinator", signatureLabelFont);
        leftLabel.setAlignment(Element.ALIGN_CENTER);
        leftLabel.setSpacingBefore(5f);
        left.addElement(leftLabel);
        
        // Right signature
        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_CENTER);
        right.setPaddingTop(10f);
        
        Paragraph rightLine = new Paragraph("_____________________________", signatureFont);
        rightLine.setAlignment(Element.ALIGN_CENTER);
        right.addElement(rightLine);
        
        Paragraph rightLabel = new Paragraph("Program Director", signatureLabelFont);
        rightLabel.setAlignment(Element.ALIGN_CENTER);
        rightLabel.setSpacingBefore(5f);
        right.addElement(rightLabel);
        
        signatures.addCell(left);
        signatures.addCell(right);
        document.add(signatures);
    }

    private void addFooterWithQR(Document document, String verificationText) throws Exception {
        Font smallMuted = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(148, 163, 184));
        
        Paragraph verification = new Paragraph(verificationText, smallMuted);
        verification.setAlignment(Element.ALIGN_LEFT);
        
        Image qrImage = generateQrImage(verificationText, 120, 120);
        
        PdfPTable footer = new PdfPTable(2);
        footer.setWidthPercentage(100);
        footer.setSpacingBefore(15f);
        
        PdfPCell verificationCell = new PdfPCell(verification);
        verificationCell.setBorder(Rectangle.NO_BORDER);
        verificationCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        verificationCell.setPaddingLeft(10f);
        
        PdfPCell qrCell = new PdfPCell(qrImage, false);
        qrCell.setBorder(Rectangle.NO_BORDER);
        qrCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        qrCell.setPaddingRight(10f);
        
        footer.addCell(verificationCell);
        footer.addCell(qrCell);
        document.add(footer);
    }

    private void addLogoIfExists(Document document) {
        try {
            ClassPathResource logoResource = new ClassPathResource("static/forme-logo.png");
            if (!logoResource.exists()) {
                return;
            }

            File tempFile = File.createTempFile("forme-logo", ".png");
            try (InputStream in = logoResource.getInputStream()) {
                Files.copy(in, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            Image logo = Image.getInstance(tempFile.getAbsolutePath());
            // Scale logo to fit nicely at the top of the certificate
            logo.scaleToFit(180, 80);
            logo.setAlignment(Element.ALIGN_CENTER);
            logo.setSpacingAfter(10f);
            document.add(logo);
            tempFile.delete();
        } catch (Exception ignored) {
            // Logo is optional; document generation still succeeds.
        }
    }

    private Image generateQrImage(String text, int width, int height) throws WriterException, java.io.IOException {
        QRCodeWriter qrWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix matrix = qrWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            byte[] pngData = baos.toByteArray();
            Image image = Image.getInstance(pngData);
            image.scaleToFit(80, 80);
            return image;
        }
    }
}
