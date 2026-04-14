package tn.esprit.document.documentservice.modules.document.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.document.documentservice.modules.document.entity.Document;
import tn.esprit.document.documentservice.modules.document.service.IDocumentService;

import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.MalformedURLException;
import java.io.IOException;
import java.util.stream.Stream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/documents")
@AllArgsConstructor
public class DocumentController {

    private final IDocumentService documentService;

    @GetMapping
    public List<Document> listDocuments(@RequestParam(required = false) String q) {
        // Simple search logic if needed, currently returning all
        return documentService.getAllDocuments();
    }

    @PostMapping
    public Document uploadDocument(
            @RequestParam("title") String title,
            @RequestParam("formationId") Long formationId,
            @RequestParam("ownerId") Long ownerId,
            @RequestParam("ownerType") String ownerType,
            @RequestParam("file") MultipartFile file) {
        return documentService.uploadDocument(title, formationId, ownerId, ownerType, file);
    }

    @GetMapping("/{id}")
    public Document getDocument(@PathVariable Long id) {
        return documentService.getDocument(id);
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public Document updateDocumentMultipart(
            @PathVariable Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "formationId", required = false) Long formationId,
            @RequestParam(value = "ownerId", required = false) Long ownerId,
            @RequestParam(value = "ownerType", required = false) String ownerType,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        return documentService.updateDocument(id, title, formationId, ownerId, ownerType, file);
    }
    
    @PutMapping(value = "/{id}", consumes = {"application/json"})
    public Document updateDocumentJson(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        String title = payload.containsKey("title") ? (String) payload.get("title") : null;
        Long formationId = payload.containsKey("formationId") ? ((Number) payload.get("formationId")).longValue() : null;
        Long ownerId = payload.containsKey("ownerId") ? ((Number) payload.get("ownerId")).longValue() : null;
        String ownerType = payload.containsKey("ownerType") ? (String) payload.get("ownerType") : null;
        return documentService.updateDocument(id, title, formationId, ownerId, ownerType, null);
    }

    @DeleteMapping("/{id}")
    public void deleteDocument(
            @PathVariable Long id,
            @RequestParam Long requesterId,
            @RequestParam(defaultValue = "false") boolean isSuperAdmin) {
        documentService.deleteDocument(id, requesterId, isSuperAdmin);
    }

    @GetMapping("/formation/{formationId}")
    public List<Document> getDocumentsByFormationId(@PathVariable Long formationId) {
        return documentService.getDocumentsByFormationId(formationId);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> downloadDocumentFile(@PathVariable Long id) {
        Document doc = documentService.getDocument(id);
        if (doc == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            Path file = resolveStoredFilePath(doc.getFilePath());
            if (file == null) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/{id}/preview", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> previewDocument(@PathVariable Long id) {
        Document doc = documentService.getDocument(id);
        if (doc == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path file = resolveStoredFilePath(doc.getFilePath());
            if (file == null) {
                return ResponseEntity.notFound().build();
            }

            String preview = extractPreviewText(file, doc.getFileName());
            if (preview == null || preview.isBlank()) {
                preview = "No readable text preview is available for this file.";
            }
            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to generate preview: " + e.getMessage());
        }
    }

    @PostMapping(value = "/{id}/ask", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> askDocument(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        Document doc = documentService.getDocument(id);
        if (doc == null) {
            return ResponseEntity.notFound().build();
        }

        String question = payload != null ? payload.get("question") : null;
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Question is required"));
        }

        try {
            Path file = resolveStoredFilePath(doc.getFilePath());
            if (file == null) {
                return ResponseEntity.notFound().build();
            }

            String content = extractPreviewText(file, doc.getFileName());
            if (content == null || content.isBlank()) {
                return ResponseEntity.ok(Map.of(
                        "answer", "I could not extract readable content from this file yet.",
                        "snippets", List.of()
                ));
            }

            Map<String, Object> result = answerFromContent(question, content);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to answer question: " + e.getMessage()));
        }
    }

    private Path resolveStoredFilePath(String storedFilePath) {
        if (storedFilePath == null || storedFilePath.isBlank()) {
            return null;
        }

        for (Path uploadDir : candidateUploadDirs()) {
            Path exact = uploadDir.resolve(storedFilePath).normalize();
            if (Files.exists(exact) && Files.isReadable(exact)) {
                return exact;
            }

            String prefix = extractUuidPrefix(storedFilePath);
            if (prefix == null) {
                continue;
            }

            try (Stream<Path> files = Files.list(uploadDir)) {
                Path fallback = files
                        .filter(p -> p.getFileName().toString().startsWith(prefix + "_"))
                        .findFirst()
                        .orElse(null);
                if (fallback != null && Files.isReadable(fallback)) {
                    return fallback;
                }
            } catch (IOException ignored) {
                // Try next upload dir candidate.
            }
        }

        return null;
    }

    private String extractUuidPrefix(String value) {
        int i = value.indexOf('_');
        if (i <= 0) return null;
        return value.substring(0, i);
    }

    private List<Path> candidateUploadDirs() {
        String userDir = System.getProperty("user.dir", ".");
        Path cwd = Paths.get(userDir).toAbsolutePath().normalize();

        List<Path> dirs = new ArrayList<>();
        dirs.add(Paths.get("uploads").toAbsolutePath().normalize());
        dirs.add(cwd.resolve("uploads").normalize());
        dirs.add(cwd.resolve("document-service").resolve("uploads").normalize());
        dirs.add(cwd.resolve("FormeBack").resolve("document-service").resolve("uploads").normalize());

        for (int level = 0; level < 5 && cwd != null; level++) {
            dirs.add(cwd.resolve("uploads").normalize());
            dirs.add(cwd.resolve("document-service").resolve("uploads").normalize());
            dirs.add(cwd.resolve("FormeBack").resolve("document-service").resolve("uploads").normalize());
            cwd = cwd.getParent();
        }

        List<Path> existing = new ArrayList<>();
        for (Path d : dirs) {
            if (Files.exists(d) && Files.isDirectory(d) && !existing.contains(d)) {
                existing.add(d);
            }
        }
        return existing;
    }

    private String extractPreviewText(Path file, String originalFileName) throws Exception {
        String lower = originalFileName == null ? "" : originalFileName.toLowerCase(Locale.ROOT);

        if (lower.endsWith(".docx")) {
            try (java.io.InputStream input = java.nio.file.Files.newInputStream(file);
                 org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument(input)) {
                StringBuilder text = new StringBuilder();
                document.getParagraphs().forEach(p -> appendLine(text, p.getText()));
                document.getTables().forEach(table -> table.getRows().forEach(row ->
                        row.getTableCells().forEach(cell -> appendLine(text, cell.getText()))));
                return text.toString();
            }
        }

        if (lower.endsWith(".pptx")) {
            try (java.io.InputStream input = java.nio.file.Files.newInputStream(file);
                 org.apache.poi.xslf.usermodel.XMLSlideShow slideshow = new org.apache.poi.xslf.usermodel.XMLSlideShow(input)) {
                StringBuilder text = new StringBuilder();
                int slideNumber = 1;
                for (org.apache.poi.xslf.usermodel.XSLFSlide slide : slideshow.getSlides()) {
                    appendLine(text, "Slide " + slideNumber++);
                    slide.getShapes().forEach(shape -> {
                        if (shape instanceof org.apache.poi.xslf.usermodel.XSLFTextShape textShape) {
                            appendLine(text, textShape.getText());
                        }
                    });
                    appendLine(text, "");
                }
                return text.toString();
            }
        }

        if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
            try (java.io.InputStream input = java.nio.file.Files.newInputStream(file);
                 org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(input)) {
                StringBuilder text = new StringBuilder();
                org.apache.poi.ss.usermodel.DataFormatter formatter = new org.apache.poi.ss.usermodel.DataFormatter();
                for (org.apache.poi.ss.usermodel.Sheet sheet : workbook) {
                    appendLine(text, "Sheet: " + sheet.getSheetName());
                    for (org.apache.poi.ss.usermodel.Row row : sheet) {
                        StringBuilder rowText = new StringBuilder();
                        for (org.apache.poi.ss.usermodel.Cell cell : row) {
                            String value = formatter.formatCellValue(cell).trim();
                            if (!value.isEmpty()) {
                                if (!rowText.isEmpty()) rowText.append(" | ");
                                rowText.append(value);
                            }
                        }
                        appendLine(text, rowText.toString());
                    }
                    appendLine(text, "");
                }
                return text.toString();
            }
        }

        if (lower.endsWith(".pdf")) {
            return "PDF preview is handled directly in the browser viewer.";
        }

        return "Preview not available for this file type.";
    }

    private void appendLine(StringBuilder builder, String line) {
        if (line == null || line.isBlank()) {
            builder.append(System.lineSeparator());
            return;
        }
        builder.append(line).append(System.lineSeparator());
    }
    private Map<String, Object> answerFromContent(String question, String content) {
        List<String> lines = Arrays.stream(content.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        Set<String> stop = new HashSet<>(Arrays.asList(
                "the", "a", "an", "and", "or", "to", "for", "of", "in", "on", "at", "is", "are", "was", "were",
                "what", "which", "who", "when", "where", "why", "how", "can", "could", "please",
                "le", "la", "les", "de", "du", "des", "et", "ou", "est", "dans", "pour", "sur", "avec", "que", "quoi", "comment", "pourquoi"
        ));

        Set<String> qTokens = Arrays.stream(question.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))
                .filter(t -> t.length() > 2)
                .filter(t -> !stop.contains(t))
                .collect(java.util.stream.Collectors.toSet());

        boolean wantsSummary = asksForGeneralSummary(question, qTokens);

        List<Map.Entry<String, Integer>> scored = new ArrayList<>();
        for (String line : lines) {
            if (isLowValueLine(line)) continue;
            String lower = line.toLowerCase(Locale.ROOT);
            int score = 0;
            for (String token : qTokens) {
                if (lower.contains(token)) score++;
            }
            if (line.length() >= 40) score += 1;
            if (line.contains(":")) score += 1;
            if (score > 0) {
                scored.add(Map.entry(line, score));
            }
        }

        scored.sort(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed());
        List<String> snippets = scored.stream().limit(5).map(Map.Entry::getKey).toList();

        String answer;
        if (snippets.isEmpty() || wantsSummary) {
            answer = buildSummaryAnswer(question, content, lines, snippets);
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("Based on this document, here is what I found:");
            for (String s : snippets) {
                builder.append(System.lineSeparator()).append("- ").append(s);
            }
            answer = builder.toString();
        }

        return Map.of(
                "answer", answer,
                "snippets", snippets
        );
    }

    private boolean asksForGeneralSummary(String question, Set<String> qTokens) {
        String q = question == null ? "" : question.toLowerCase(Locale.ROOT);
        if (q.contains("what is this document about") || q.contains("what does this document talk about")) return true;
        if (q.contains("about this file") || q.contains("about this document")) return true;
        if (q.contains("career") || q.contains("job") || q.contains("metier") || q.contains("carriere")) return true;
        if (q.contains("subject") || q.contains("topic") || q.contains("summary") || q.contains("resume")) return true;
        return qTokens.isEmpty();
    }

    private String buildSummaryAnswer(String question, String content, List<String> lines, List<String> snippets) {
        List<String> keyLines = pickKeyLines(lines, 4);
        List<String> keywords = extractTopKeywords(content, 6);
        String q = question == null ? "" : question.toLowerCase(Locale.ROOT);
        boolean careerQuestion = q.contains("career") || q.contains("job") || q.contains("metier") || q.contains("carriere");

        StringBuilder out = new StringBuilder();
        out.append("I understood this document and here is its main subject:").append(System.lineSeparator());
        if (!keyLines.isEmpty()) {
            out.append("- ").append(keyLines.get(0));
        } else {
            out.append("- This document contains structured learning content.");
        }

        if (keywords.size() >= 2) {
            out.append(System.lineSeparator())
                    .append("Main keywords: ")
                    .append(String.join(", ", keywords));
        }

        List<String> evidence = new ArrayList<>();
        if (!snippets.isEmpty()) {
            evidence.addAll(snippets.stream().limit(3).toList());
        } else {
            evidence.addAll(keyLines.stream().skip(1).limit(3).toList());
        }

        if (!evidence.isEmpty()) {
            out.append(System.lineSeparator())
                    .append("Relevant parts from the file:");
            for (String line : evidence) {
                out.append(System.lineSeparator()).append("- ").append(line);
            }
        }

        if (careerQuestion) {
            out.append(System.lineSeparator())
                    .append("How this helps your IT career:")
                    .append(System.lineSeparator())
                    .append("- It builds practical security knowledge you need in development, networking, and system administration.")
                    .append(System.lineSeparator())
                    .append("- It helps you recognize risks early and design safer applications and infrastructure.")
                    .append(System.lineSeparator())
                    .append("- These skills are useful for real-world roles like developer, DevOps, network engineer, and cybersecurity analyst.");
        }

        out.append(System.lineSeparator())
                .append("Ask me any specific point and I will answer from this file.");

        return out.toString();
    }

    private List<String> pickKeyLines(List<String> lines, int max) {
        Set<String> kept = new java.util.LinkedHashSet<>();
        for (String line : lines) {
            if (isLowValueLine(line)) continue;
            if (line.length() < 6) continue;
            String lower = line.toLowerCase(Locale.ROOT);
            if (lower.startsWith("slide ") || lower.startsWith("page ")) continue;
            if (line.chars().filter(Character::isLetter).count() < 4) continue;
            kept.add(line);
            if (kept.size() >= max) break;
        }
        return new ArrayList<>(kept);
    }

    private List<String> extractTopKeywords(String content, int max) {
        Set<String> stop = new HashSet<>(Arrays.asList(
                "the", "a", "an", "and", "or", "to", "for", "of", "in", "on", "at", "is", "are", "was", "were",
                "what", "which", "who", "when", "where", "why", "how", "can", "could", "please", "this", "that", "with", "from",
                "about", "into", "your", "you", "le", "la", "les", "de", "du", "des", "et", "ou", "est", "dans", "pour", "sur",
                "avec", "que", "quoi", "comment", "pourquoi", "ce", "cet", "cette", "ces", "une", "un", "aux", "par", "plus", "moins"
        ));

        Map<String, Integer> freq = new java.util.LinkedHashMap<>();
        for (String token : content.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+")) {
            if (token.length() < 4) continue;
            if (stop.contains(token)) continue;
            freq.put(token, freq.getOrDefault(token, 0) + 1);
        }

        return freq.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(max)
                .map(Map.Entry::getKey)
                .toList();
    }

    private boolean isLowValueLine(String line) {
        if (line == null) return true;
        String lower = line.trim().toLowerCase(Locale.ROOT);
        if (lower.isBlank()) return true;

        // Avoid generic headings that do not provide real content.
        if (lower.matches("^(chapitre|chapter|slide|page)\\s*\\d+.*$")) return true;
        if (lower.matches("^(plan\\s+du\\s+chapitre|fin\\s+du\\s+chapitre|introduction|conclusion)$")) return true;
        return false;
    }
}
