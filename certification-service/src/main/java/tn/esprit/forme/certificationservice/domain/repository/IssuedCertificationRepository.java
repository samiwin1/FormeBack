package tn.esprit.forme.certificationservice.domain.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.forme.certificationservice.domain.entity.IssuedCertification;
import tn.esprit.forme.certificationservice.domain.enums.IssuedCertificationStatus;

import java.util.List;
import java.util.Optional;

public interface IssuedCertificationRepository extends JpaRepository<IssuedCertification, Long> {
    List<IssuedCertification> findByLearnerId(Long learnerId);
    List<IssuedCertification> findByStatus(IssuedCertificationStatus status);
    List<IssuedCertification> findByLearnerIdAndStatus(Long learnerId, IssuedCertificationStatus status);
    List<IssuedCertification> findByFormationIdAndStatus(Long formationId, IssuedCertificationStatus status);
    List<IssuedCertification> findByLearnerIdAndFormationIdAndStatus(Long learnerId, Long formationId, IssuedCertificationStatus status);
    boolean existsByLearnerIdAndCertificationIdAndFormationId(Long learnerId, Long certificationId, Long formationId);
    Optional<IssuedCertification> findByLearnerIdAndCertificationIdAndFormationId(Long learnerId, Long certificationId, Long formationId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM IssuedCertification i WHERE i.certificateNumber LIKE :prefix% ORDER BY i.certificateNumber DESC")
    IssuedCertification findTopByCertificateNumberStartingWithOrderByCertificateNumberDescWithLock(@Param("prefix") String prefix);
    
    IssuedCertification findTopByCertificateNumberStartingWithOrderByCertificateNumberDesc(String prefix);
    long countByStatus(IssuedCertificationStatus status);
}
