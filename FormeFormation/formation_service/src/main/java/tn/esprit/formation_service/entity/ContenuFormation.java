package tn.esprit.formation_service.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "contenu_formation")
public class ContenuFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formation_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Formation formation;

    private String title;

    @Column(name = "content_type")
    private String content_type;

    @Column(name = "content_body", columnDefinition = "TEXT")
    private String content_body;

    @Column(name = "order_index")
    private Integer order_index;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Evaluation evaluation;

    @Column(name = "is_locked")
    private Boolean is_locked;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Formation getFormation() { return formation; }
    public void setFormation(Formation formation) { this.formation = formation; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent_type() { return content_type; }
    public void setContent_type(String content_type) { this.content_type = content_type; }
    public String getContent_body() { return content_body; }
    public void setContent_body(String content_body) { this.content_body = content_body; }
    public Integer getOrder_index() { return order_index; }
    public void setOrder_index(Integer order_index) { this.order_index = order_index; }
    public Evaluation getEvaluation() { return evaluation; }
    public void setEvaluation(Evaluation evaluation) { this.evaluation = evaluation; }
    public Boolean getIs_locked() { return is_locked; }
    public void setIs_locked(Boolean is_locked) { this.is_locked = is_locked; }

    @JsonProperty("formation_id")
    public void setFormationId(Long formationId) {
        if (formationId != null) {
            Formation f = new Formation();
            f.setId(formationId);
            this.formation = f;
        }
    }

    @JsonProperty("evaluation_id")
    public void setEvaluationId(Long evaluationId) {
        if (evaluationId != null) {
            Evaluation e = new Evaluation();
            e.setId(evaluationId);
            this.evaluation = e;
        }
    }

    @JsonProperty("evaluation_id")
    public Long getEvaluationId() {
        return evaluation != null ? evaluation.getId() : null;
    }
}
