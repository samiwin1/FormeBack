package tn.esprit.partnerintelligence.repository;
import org.springframework.data.jpa.repository.JpaRepository;import tn.esprit.partnerintelligence.entity.RecommendationItem;import java.util.List;public interface RecommendationItemRepository extends JpaRepository<RecommendationItem,Long>{List<RecommendationItem> findByPartnerId(Long partnerId);}
