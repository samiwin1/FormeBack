package tn.esprit.formation_service.dto;

import java.time.Instant;

public class EvaluationResponse {

    private Long id;
    private Long formation_id;
    private String title;
    private String content;
    private String evaluation_type;
    private Integer passing_score;
    private Integer max_attempts;
    private String linked_content_range;
    private Instant created_at;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFormation_id() { return formation_id; }
    public void setFormation_id(Long formation_id) { this.formation_id = formation_id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getEvaluation_type() { return evaluation_type; }
    public void setEvaluation_type(String evaluation_type) { this.evaluation_type = evaluation_type; }
    public Integer getPassing_score() { return passing_score; }
    public void setPassing_score(Integer passing_score) { this.passing_score = passing_score; }
    public Integer getMax_attempts() { return max_attempts; }
    public void setMax_attempts(Integer max_attempts) { this.max_attempts = max_attempts; }
    public String getLinked_content_range() { return linked_content_range; }
    public void setLinked_content_range(String linked_content_range) { this.linked_content_range = linked_content_range; }
    public Instant getCreated_at() { return created_at; }
    public void setCreated_at(Instant created_at) { this.created_at = created_at; }
}
