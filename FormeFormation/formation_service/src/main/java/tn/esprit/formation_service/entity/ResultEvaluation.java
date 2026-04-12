package tn.esprit.formation_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "result_evaluation")
public class ResultEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    @JsonIgnore
    private Evaluation evaluation;

    @Column(name = "user_id", nullable = false)
    private Long user_id;

    private Integer score;

    @Column(name = "attempt_number")
    private Integer attempt_number;

    private Boolean passed;

    @Column(name = "answered_at")
    private Instant answered_at;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Evaluation getEvaluation() { return evaluation; }
    public void setEvaluation(Evaluation evaluation) { this.evaluation = evaluation; }
    public Long getUser_id() { return user_id; }
    public void setUser_id(Long user_id) { this.user_id = user_id; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getAttempt_number() { return attempt_number; }
    public void setAttempt_number(Integer attempt_number) { this.attempt_number = attempt_number; }
    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }
    public Instant getAnswered_at() { return answered_at; }
    public void setAnswered_at(Instant answered_at) { this.answered_at = answered_at; }

    @JsonProperty("evaluation_id")
    public Long getEvaluationId() {
        return evaluation != null ? evaluation.getId() : null;
    }
}
