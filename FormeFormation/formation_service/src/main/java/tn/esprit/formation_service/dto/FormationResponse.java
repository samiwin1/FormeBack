package tn.esprit.formation_service.dto;

import java.time.Instant;

public class FormationResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private String level;
    private String objectives;
    private String skills_targeted;
    private String status;
    private Long created_by;
    private Instant created_at;
    private Instant updated_at;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getObjectives() { return objectives; }
    public void setObjectives(String objectives) { this.objectives = objectives; }
    public String getSkills_targeted() { return skills_targeted; }
    public void setSkills_targeted(String skills_targeted) { this.skills_targeted = skills_targeted; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getCreated_by() { return created_by; }
    public void setCreated_by(Long created_by) { this.created_by = created_by; }
    public Instant getCreated_at() { return created_at; }
    public void setCreated_at(Instant created_at) { this.created_at = created_at; }
    public Instant getUpdated_at() { return updated_at; }
    public void setUpdated_at(Instant updated_at) { this.updated_at = updated_at; }
}
