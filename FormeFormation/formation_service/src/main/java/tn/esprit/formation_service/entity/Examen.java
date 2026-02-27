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
@Table(name = "examen")
@EntityListeners(AuditingEntityListener.class)
public class Examen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formation_id", nullable = false, unique = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Formation formation;

    private String title;

    @Column(name = "duration_minutes")
    private Integer duration_minutes;

    @Column(name = "passing_score")
    private Integer passing_score;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_by")
    private Long created_by;

    @CreatedDate
    @Column(name = "created_at")
    private Instant created_at;

    @OneToMany(mappedBy = "examen", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ResultExamen> resultExamens = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Formation getFormation() { return formation; }
    public void setFormation(Formation formation) { this.formation = formation; }
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
    public Instant getCreated_at() { return created_at; }
    public void setCreated_at(Instant created_at) { this.created_at = created_at; }
    public List<ResultExamen> getResultExamens() { return resultExamens; }
    public void setResultExamens(List<ResultExamen> resultExamens) { this.resultExamens = resultExamens; }

    @JsonProperty("formation_id")
    public void setFormationId(Long formationId) {
        if (formationId != null) {
            Formation f = new Formation();
            f.setId(formationId);
            this.formation = f;
        }
    }
}
