package tn.esprit.formation_service.dto;

import java.time.Instant;

public class ResultEvaluationRequest {

    private Long evaluation_id;
    private Long user_id;
    private Integer score;
    private Integer attempt_number;
    private Boolean passed;
    private Instant answered_at;

    public Long getEvaluation_id() { return evaluation_id; }
    public void setEvaluation_id(Long evaluation_id) { this.evaluation_id = evaluation_id; }
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
}
