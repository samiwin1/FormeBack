package tn.esprit.formation_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evaluation")
@EntityListeners(AuditingEntityListener.class)
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formation_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Formation formation;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "evaluation_type")
    private String evaluation_type;

    @Column(name = "passing_score")
    private Integer passing_score;

    @Column(name = "max_attempts")
    private Integer max_attempts;

    @Column(name = "linked_content_range")
    private String linked_content_range;

    @CreatedDate
    @Column(name = "created_at")
    private Instant created_at;

    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ResultEvaluation> resultEvaluations = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Formation getFormation() { return formation; }
    public void setFormation(Formation formation) { this.formation = formation; }
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
    public List<ResultEvaluation> getResultEvaluations() { return resultEvaluations; }
    public void setResultEvaluations(List<ResultEvaluation> resultEvaluations) { this.resultEvaluations = resultEvaluations; }

    @JsonProperty("formation_id")
    public void setFormationId(Long formationId) {
        if (formationId != null) {
            Formation f = new Formation();
            f.setId(formationId);
            this.formation = f;
        }
    }
}
