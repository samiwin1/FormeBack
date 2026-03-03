package tn.esprit.formation_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.formation_service.entity.ResultExamen;

import java.util.List;
import java.util.Optional;

public interface ResultExamenRepository extends JpaRepository<ResultExamen, Long> {

    List<ResultExamen> findByExamen_Id(Long examenId);

    @Query("SELECT r FROM ResultExamen r LEFT JOIN FETCH r.examen WHERE r.user_id = :userId")
    List<ResultExamen> findByUser_id(@Param("userId") Long user_id);

    @Query("SELECT r FROM ResultExamen r WHERE r.examen.id = :examenId AND r.user_id = :userId AND r.end_time IS NULL")
    Optional<ResultExamen> findByExamen_IdAndUser_idAndEnd_timeIsNull(@Param("examenId") Long examenId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM ResultExamen r WHERE r.user_id = :userId AND r.examen.formation.id = :formationId")
    int deleteByUser_idAndExamen_Formation_Id(@Param("userId") Long userId, @Param("formationId") Long formationId);

    @Query("SELECT COUNT(r) FROM ResultExamen r WHERE r.end_time IS NOT NULL")
    long countCompletedExams();

    @Query("SELECT COUNT(r) FROM ResultExamen r WHERE r.end_time IS NOT NULL AND r.passed = true")
    long countPassedExams();

    @Query("SELECT AVG(r.score) FROM ResultExamen r WHERE r.end_time IS NOT NULL AND r.score IS NOT NULL")
    Double averageScoreCompleted();

    @Query(value = "SELECT AVG(TIMESTAMPDIFF(MINUTE, start_time, end_time)) FROM result_examen WHERE end_time IS NOT NULL", nativeQuery = true)
    Double averageCompletionMinutes();

    @Query("SELECT r.examen.id, r.user_id, COUNT(r) FROM ResultExamen r WHERE r.passed = false " +
           "AND r.end_time IS NOT NULL GROUP BY r.examen.id, r.user_id HAVING COUNT(r) >= 3")
    List<Object[]> findRepeatedFailures();

    @Query("SELECT r.examen.id, AVG(r.score) FROM ResultExamen r WHERE r.end_time IS NOT NULL AND r.score IS NOT NULL " +
           "GROUP BY r.examen.id")
    List<Object[]> findExamAverageScores();

    @Query(value = "SELECT examen_id, AVG(TIMESTAMPDIFF(MINUTE, start_time, end_time)) FROM result_examen " +
           "WHERE end_time IS NOT NULL GROUP BY examen_id", nativeQuery = true)
    List<Object[]> findExamAverageDurations();

    @Query("SELECT DISTINCT r.examen.formation.id, r.user_id FROM ResultExamen r WHERE r.end_time IS NOT NULL")
    List<Object[]> findDistinctFormationUserPairsFromExams();

    @Query("SELECT DISTINCT r.examen.formation.id, r.user_id FROM ResultExamen r")
    List<Object[]> findDistinctFormationUserPairsFromExamsAny();
}
