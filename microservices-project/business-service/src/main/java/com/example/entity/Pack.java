package com.example.entity;

import jakarta.persistence.*;

@Entity
public class Pack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private int validityMonths;
    private boolean active;

    public Pack() {
    }

    public Pack(String name, String description, int validityMonths, boolean active) {
        this.name = name;
        this.description = description;
        this.validityMonths = validityMonths;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getValidityMonths() {
        return validityMonths;
    }

    public void setValidityMonths(int validityMonths) {
        this.validityMonths = validityMonths;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
