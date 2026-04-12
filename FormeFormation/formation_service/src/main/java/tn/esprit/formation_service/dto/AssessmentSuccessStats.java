package tn.esprit.formation_service.dto;

import java.util.List;

public class AssessmentSuccessStats {

    private long totalAttempts;
    private long passedAttempts;
    private double successRatePercent;
    private double averageScore;
    private List<EvaluationFailureItem> evaluationsWithHighFailure;

    public AssessmentSuccessStats() {
    }

    public AssessmentSuccessStats(long totalAttempts, long passedAttempts, double successRatePercent,
                                  double averageScore, List<EvaluationFailureItem> evaluationsWithHighFailure) {
        this.totalAttempts = totalAttempts;
        this.passedAttempts = passedAttempts;
        this.successRatePercent = successRatePercent;
        this.averageScore = averageScore;
        this.evaluationsWithHighFailure = evaluationsWithHighFailure;
    }

    public long getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(long totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public long getPassedAttempts() {
        return passedAttempts;
    }

    public void setPassedAttempts(long passedAttempts) {
        this.passedAttempts = passedAttempts;
    }

    public double getSuccessRatePercent() {
        return successRatePercent;
    }

    public void setSuccessRatePercent(double successRatePercent) {
        this.successRatePercent = successRatePercent;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public List<EvaluationFailureItem> getEvaluationsWithHighFailure() {
        return evaluationsWithHighFailure;
    }

    public void setEvaluationsWithHighFailure(List<EvaluationFailureItem> evaluationsWithHighFailure) {
        this.evaluationsWithHighFailure = evaluationsWithHighFailure;
    }
}
