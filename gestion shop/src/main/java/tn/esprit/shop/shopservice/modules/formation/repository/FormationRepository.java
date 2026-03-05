package tn.esprit.shop.shopservice.modules.formation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.shop.shopservice.modules.formation.entity.Formation;
import tn.esprit.shop.shopservice.modules.formation.entity.FormationStatus;

import java.util.List;

@Repository
public interface FormationRepository extends JpaRepository<Formation, Long> {
    List<Formation> findByStatus(FormationStatus status);
    List<Formation> findByCreatedBy(Long createdBy);
    List<Formation> findByCategory(String category);
}
