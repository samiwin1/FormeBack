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
        document.setOwnerType(OwnerType.valueOf(ownerType));

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

        return documentRepository.save(document);
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
        if (ownerType != null) document.setOwnerType(OwnerType.valueOf(ownerType));

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
        return documentRepository.save(document);
    }

    @Override
    public void deleteDocument(Long id) {
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
            return FileType.OTHER;
        }

        String name = originalFilename.toLowerCase(Locale.ROOT);
        if (name.endsWith(".pdf")) return FileType.PDF;
        if (name.endsWith(".docx")) return FileType.DOCX;
        if (name.endsWith(".doc")) return FileType.DOC;
        if (name.endsWith(".pptx")) return FileType.PPTX;
        if (name.endsWith(".ppt")) return FileType.PPT;
        if (name.endsWith(".xlsx")) return FileType.XLSX;
        if (name.endsWith(".xls")) return FileType.XLS;
        if (name.endsWith(".odt")) return FileType.ODT;
        if (name.endsWith(".ods")) return FileType.ODS;
        if (name.endsWith(".odp")) return FileType.ODP;
        if (name.endsWith(".txt")) return FileType.TEXT;
        if (name.matches(".*\\.(mp4|avi|mov|mkv|webm)$")) return FileType.VIDEO;
        if (name.matches(".*\\.(png|jpg|jpeg|gif|webp|bmp|svg)$")) return FileType.IMAGE;
        return FileType.OTHER;
    }
}
