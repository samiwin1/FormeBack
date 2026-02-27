package tn.esprit.formation_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.formation_service.entity.ResultExamen;

import java.util.List;

public interface ResultExamenRepository extends JpaRepository<ResultExamen, Long> {

    List<ResultExamen> findByExamen_Id(Long examenId);

    @Query("SELECT r FROM ResultExamen r WHERE r.user_id = :userId")
    List<ResultExamen> findByUser_id(@Param("userId") Long user_id);
}
