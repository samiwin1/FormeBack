package tn.esprit.forme.certificationservice.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.forme.certificationservice.domain.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndReadFalse(Long userId);
}
