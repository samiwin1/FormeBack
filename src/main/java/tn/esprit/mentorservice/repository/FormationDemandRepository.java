package tn.esprit.mentorservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.mentorservice.entity.FormationDemand;

import java.util.List;

@Repository
public interface FormationDemandRepository extends JpaRepository<FormationDemand, Long> {

    List<FormationDemand> findAllByOrderByCreatedAtDesc();

    List<FormationDemand> findByRequestedRoleIgnoreCaseOrderByCreatedAtDesc(String requestedRole);

    @Query("SELECT f.requestedRole, COUNT(f) FROM FormationDemand f " +
           "WHERE f.requestedRole IS NOT NULL " +
           "GROUP BY f.requestedRole ORDER BY COUNT(f) DESC")
    List<Object[]> countByRole();
}
