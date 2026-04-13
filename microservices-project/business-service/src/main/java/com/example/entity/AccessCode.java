package com.example.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class AccessCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private Long partnerId;
    private Long dealId;
    private LocalDate expirationDate;
    private boolean used;

    public AccessCode() {
    }

    public AccessCode(String code, Long partnerId, Long dealId, LocalDate expirationDate, boolean used) {
        this.code = code;
        this.partnerId = partnerId;
        this.dealId = dealId;
        this.expirationDate = expirationDate;
        this.used = used;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Long partnerId) {
        this.partnerId = partnerId;
    }

    public Long getDealId() {
        return dealId;
    }

    public void setDealId(Long dealId) {
        this.dealId = dealId;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
