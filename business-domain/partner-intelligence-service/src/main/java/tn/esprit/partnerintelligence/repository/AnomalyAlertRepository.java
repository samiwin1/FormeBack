package tn.esprit.partnerintelligence.repository;
import org.springframework.data.jpa.repository.JpaRepository;import tn.esprit.partnerintelligence.entity.AnomalyAlert;import java.util.List;public interface AnomalyAlertRepository extends JpaRepository<AnomalyAlert,Long>{List<AnomalyAlert> findByStatus(String status);}
