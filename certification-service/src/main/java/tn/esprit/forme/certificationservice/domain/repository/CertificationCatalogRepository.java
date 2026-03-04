package tn.esprit.forme.certificationservice.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.forme.certificationservice.domain.entity.CertificationCatalog;

public interface CertificationCatalogRepository extends JpaRepository<CertificationCatalog, Long> {
}
