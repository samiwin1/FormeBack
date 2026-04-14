package tn.esprit.document.documentservice.modules.document.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.document.documentservice.modules.document.entity.Document;
import tn.esprit.document.documentservice.modules.document.entity.FileType;
import tn.esprit.document.documentservice.modules.document.entity.OwnerType;
import tn.esprit.document.documentservice.modules.document.repository.DocumentRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class DocumentService implements IDocumentService {

    private final DocumentRepository documentRepository;
    private final Path storageLocation = Paths.get("uploads");

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
        try {
            Files.createDirectories(storageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public Document uploadDocument(String title, Long formationId, Long ownerId, String ownerType, MultipartFile file) {
        Document document = new Document();
        document.setTitle(title);
        document.setFormationId(formationId);
        document.setOwnerId(ownerId);
        document.setOwnerType(parseOwnerType(ownerType));

        if (file != null && !file.isEmpty()) {
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            try {
                Files.copy(file.getInputStream(), this.storageLocation.resolve(filename));
                document.setFileName(file.getOriginalFilename());
                document.setFilePath(filename);
                document.setFileType(detectFileType(file.getOriginalFilename()));

            } catch (IOException e) {
                throw new RuntimeException("Failed to store file.", e);
            }
        }

        return saveDocumentWithSchemaFallback(document);
    }

    @Override
    public Document getDocument(Long id) {
        return documentRepository.findById(id).orElse(null);
    }

    @Override
    public Document updateDocument(Long id, String title, Long formationId, Long ownerId, String ownerType, MultipartFile file) {
        Document document = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Document not found"));
        if (title != null) document.setTitle(title);
        if (formationId != null) document.setFormationId(formationId);
        if (ownerId != null) document.setOwnerId(ownerId);
        if (ownerType != null) document.setOwnerType(parseOwnerType(ownerType));

        if (file != null && !file.isEmpty()) {
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            try {
                Files.copy(file.getInputStream(), this.storageLocation.resolve(filename));
                document.setFileName(file.getOriginalFilename());
                document.setFilePath(filename);
                document.setFileType(detectFileType(file.getOriginalFilename()));

            } catch (IOException e) {
                throw new RuntimeException("Failed to store file.", e);
            }
        }
        return saveDocumentWithSchemaFallback(document);
    }

    @Override
    public void deleteDocument(Long id, Long requesterId, boolean isSuperAdmin) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        boolean ownerDelete = requesterId != null && requesterId.equals(document.getOwnerId());
        if (!isSuperAdmin && !ownerDelete) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only super admin or document owner can delete this document");
        }

        documentRepository.deleteById(id);
    }

    @Override
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    @Override
    public List<Document> getDocumentsByFormationId(Long formationId) {
        return documentRepository.findByFormationId(formationId);
    }

    private FileType detectFileType(String originalFilename) {
        if (originalFilename == null) {
            return FileType.IMAGE;
        }

        String name = originalFilename.toLowerCase(Locale.ROOT);
        if (name.endsWith(".pdf")) return FileType.PDF;
        if (name.endsWith(".docx")) return FileType.DOCX;
        if (name.endsWith(".doc")) return FileType.DOC;
        return FileType.IMAGE;
    }

    private OwnerType parseOwnerType(String ownerType) {
        if (ownerType == null || ownerType.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ownerType is required");
        }
        try {
            return OwnerType.valueOf(ownerType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ownerType: " + ownerType);
        }
    }

    private Document saveDocumentWithSchemaFallback(Document document) {
        try {
            return documentRepository.save(document);
        } catch (DataIntegrityViolationException ex) {
            FileType original = document.getFileType();
            if (original != FileType.IMAGE) {
                log.warn("fileType {} rejected by current schema, retrying with IMAGE", original);
                document.setFileType(FileType.IMAGE);
                try {
                    return documentRepository.save(document);
                } catch (DataIntegrityViolationException retryEx) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "File type is not supported by the current database schema", retryEx);
                }
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not save document due to schema constraints", ex);
        }
    }
}
