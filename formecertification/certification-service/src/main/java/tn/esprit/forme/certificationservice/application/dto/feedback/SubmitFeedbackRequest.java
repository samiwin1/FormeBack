package tn.esprit.forme.certificationservice.application.dto.feedback;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class SubmitFeedbackRequest {

    @Min(1)
    @Max(5)
    private int sessionRating;

    @Min(1)
    @Max(5)
    private int evaluatorRating;

    @Size(max = 500)
    private String comment;

    @NotNull
    private Long issuedCertificationId;

    @NotNull
    private Long sessionId;

    public int getSessionRating() {
        return sessionRating;
    }

    public void setSessionRating(int sessionRating) {
        this.sessionRating = sessionRating;
    }

    public int getEvaluatorRating() {
        return evaluatorRating;
    }

    public void setEvaluatorRating(int evaluatorRating) {
        this.evaluatorRating = evaluatorRating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getIssuedCertificationId() {
        return issuedCertificationId;
    }

    public void setIssuedCertificationId(Long issuedCertificationId) {
        this.issuedCertificationId = issuedCertificationId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
}

