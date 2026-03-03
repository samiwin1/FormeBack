package tn.esprit.formation_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "result_examen")
public class ResultExamen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examen_id", nullable = false)
    @JsonIgnore
    private Examen examen;

    @Column(name = "user_id", nullable = false)
    private Long user_id;

    @Column(name = "start_time")
    private Instant start_time;

    @Column(name = "end_time")
    private Instant end_time;

    @Column(name = "submitted_answers", columnDefinition = "TEXT")
    private String submitted_answers;

    private Integer score;
    private Boolean passed;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Examen getExamen() { return examen; }
    public void setExamen(Examen examen) { this.examen = examen; }
    public Long getUser_id() { return user_id; }
    public void setUser_id(Long user_id) { this.user_id = user_id; }
    public Instant getStart_time() { return start_time; }
    public void setStart_time(Instant start_time) { this.start_time = start_time; }
    public Instant getEnd_time() { return end_time; }
    public void setEnd_time(Instant end_time) { this.end_time = end_time; }
    public String getSubmitted_answers() { return submitted_answers; }
    public void setSubmitted_answers(String submitted_answers) { this.submitted_answers = submitted_answers; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }

    @JsonProperty("examen_id")
    public Long getExamenId() {
        return examen != null ? examen.getId() : null;
    }
}
