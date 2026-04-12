package tn.esprit.document.documentservice.modules.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.document.documentservice.modules.document.entity.Document;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByFormationId(Long formationId);
}
