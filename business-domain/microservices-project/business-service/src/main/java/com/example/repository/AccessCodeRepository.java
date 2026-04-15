package com.example.repository;

import com.example.entity.AccessCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessCodeRepository extends JpaRepository<AccessCode, Long> {

    AccessCode findByCode(String code);

    @Query("SELECT COUNT(a) FROM AccessCode a WHERE a.partnerId = :partnerId AND a.used = true")
    Long countUsedCodes(Long partnerId);
}