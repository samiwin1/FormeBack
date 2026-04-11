package tn.esprit.mentorservice.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.mentorservice.entity.LearnerPortfolio;

import java.util.List;
import java.util.Optional;

public interface LearnerPortfolioRepository extends JpaRepository<LearnerPortfolio, Long> {

    @EntityGraph(attributePaths = "skills")
    Optional<LearnerPortfolio> findWithSkillsByUserId(Long userId);

    Optional<LearnerPortfolio> findByUserId(Long userId);

    @Query("SELECT DISTINCT lp.userId FROM LearnerPortfolio lp ORDER BY lp.createdAt DESC")
    List<Long> findAllUserIdsWithPortfolio();
}
