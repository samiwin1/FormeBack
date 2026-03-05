package com.example.repository;

import com.example.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {

    // Nombre total de deals d’un partner
    @Query("SELECT COUNT(d) FROM Deal d WHERE d.partnerId = :partnerId")
    Long countDealsByPartner(Long partnerId);

}