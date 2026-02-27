package tn.esprit.formation_service.dto;

import java.time.Instant;

public class ResultExamenRequest {

    private Long examen_id;
    private Long user_id;
    private Instant start_time;
    private Instant end_time;
    private String submitted_answers;
    private Integer score;
    private Boolean passed;

    public Long getExamen_id() { return examen_id; }
    public void setExamen_id(Long examen_id) { this.examen_id = examen_id; }
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
}
