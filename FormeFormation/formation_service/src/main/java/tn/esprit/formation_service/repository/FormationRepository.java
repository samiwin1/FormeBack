package tn.esprit.formation_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.formation_service.entity.Formation;

public interface FormationRepository extends JpaRepository<Formation, Long> {

    @Query("SELECT f FROM Formation f WHERE (:status IS NULL OR f.status = :status) " +
            "AND (:category IS NULL OR f.category = :category) AND (:level IS NULL OR f.level = :level) " +
            "AND (:search IS NULL OR :search = '' OR LOWER(f.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(f.description) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(f.category) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Formation> findByFilters(@Param("status") String status, @Param("category") String category,
                                   @Param("level") String level, @Param("search") String search, Pageable pageable);
}
