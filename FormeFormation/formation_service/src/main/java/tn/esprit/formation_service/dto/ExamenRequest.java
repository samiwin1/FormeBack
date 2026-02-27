package tn.esprit.formation_service.dto;

public class ExamenRequest {

    private Long formation_id;
    private String title;
    private Integer duration_minutes;
    private Integer passing_score;
    private String content;
    private Long created_by;

    public Long getFormation_id() { return formation_id; }
    public void setFormation_id(Long formation_id) { this.formation_id = formation_id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getDuration_minutes() { return duration_minutes; }
    public void setDuration_minutes(Integer duration_minutes) { this.duration_minutes = duration_minutes; }
    public Integer getPassing_score() { return passing_score; }
    public void setPassing_score(Integer passing_score) { this.passing_score = passing_score; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getCreated_by() { return created_by; }
    public void setCreated_by(Long created_by) { this.created_by = created_by; }
}
