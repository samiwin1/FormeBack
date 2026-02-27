package tn.esprit.formation_service.dto;

public class EvaluationSubmitResponse {

    private int score;
    private boolean passed;
    private int remainingAttempts;
    private int attemptNumber;

    public EvaluationSubmitResponse() {
    }

    public EvaluationSubmitResponse(int score, boolean passed, int remainingAttempts, int attemptNumber) {
        this.score = score;
        this.passed = passed;
        this.remainingAttempts = remainingAttempts;
        this.attemptNumber = attemptNumber;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public void setRemainingAttempts(int remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }
}
