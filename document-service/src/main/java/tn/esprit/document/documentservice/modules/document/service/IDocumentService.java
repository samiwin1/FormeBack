package tn.esprit.document.documentservice.modules.document.service;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.document.documentservice.modules.document.entity.Document;
import java.util.List;

public interface IDocumentService {
    Document uploadDocument(String title, Long formationId, Long ownerId, String ownerType, MultipartFile file);
    Document getDocument(Long id);
    Document updateDocument(Long id, String title, Long formationId, Long ownerId, String ownerType, MultipartFile file);
    void deleteDocument(Long id, Long requesterId, boolean isSuperAdmin);
    List<Document> getAllDocuments();
    List<Document> getDocumentsByFormationId(Long formationId);
}
