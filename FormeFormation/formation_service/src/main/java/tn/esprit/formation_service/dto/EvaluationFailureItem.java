package tn.esprit.formation_service.dto;

public class EvaluationFailureItem {

    private Long evaluationId;
    private String evaluationTitle;
    private String formationTitle;
    private double failureRate;
    private long attemptCount;

    public EvaluationFailureItem() {
    }

    public EvaluationFailureItem(Long evaluationId, String evaluationTitle, String formationTitle,
                                 double failureRate, long attemptCount) {
        this.evaluationId = evaluationId;
        this.evaluationTitle = evaluationTitle;
        this.formationTitle = formationTitle;
        this.failureRate = failureRate;
        this.attemptCount = attemptCount;
    }

    public Long getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(Long evaluationId) {
        this.evaluationId = evaluationId;
    }

    public String getEvaluationTitle() {
        return evaluationTitle;
    }

    public void setEvaluationTitle(String evaluationTitle) {
        this.evaluationTitle = evaluationTitle;
    }

    public String getFormationTitle() {
        return formationTitle;
    }

    public void setFormationTitle(String formationTitle) {
        this.formationTitle = formationTitle;
    }

    public double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(double failureRate) {
        this.failureRate = failureRate;
    }

    public long getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(long attemptCount) {
        this.attemptCount = attemptCount;
    }
}
