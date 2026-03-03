package tn.esprit.formation_service.dto;

public class GenerateFormationRequest {

    private String title;
    private String description;
    private String objectives;
    private String level;
    private String skillsTargeted;
    private Integer numberOfContentBlocks;
    private Long createdBy;

    public GenerateFormationRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getObjectives() {
        return objectives;
    }

    public void setObjectives(String objectives) {
        this.objectives = objectives;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSkillsTargeted() {
        return skillsTargeted;
    }

    public void setSkillsTargeted(String skillsTargeted) {
        this.skillsTargeted = skillsTargeted;
    }

    public Integer getNumberOfContentBlocks() {
        return numberOfContentBlocks;
    }

    public void setNumberOfContentBlocks(Integer numberOfContentBlocks) {
        this.numberOfContentBlocks = numberOfContentBlocks;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}
