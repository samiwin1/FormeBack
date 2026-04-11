package tn.esprit.mentorservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.mentorservice.entity.StudyPlanItem;

import java.util.Optional;

@Repository
public interface StudyPlanItemRepository extends JpaRepository<StudyPlanItem, Long> {

    /** Returns the item only if it belongs to the authenticated user (ownership check via study_plan.user_id). */
    @Query("SELECT i FROM StudyPlanItem i WHERE i.id = :itemId AND i.studyPlan.userId = :userId")
    Optional<StudyPlanItem> findByIdAndUserId(@Param("itemId") Long itemId, @Param("userId") Long userId);
}
