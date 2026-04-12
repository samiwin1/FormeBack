package tn.esprit.formation_service.dto;

import java.util.List;

public class FormationProgressResponse {

    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";

    private List<FormationProgressContentItem> content;
    private boolean examEligible;
    private double completionPercentage;
    private String formationStatus;

    public FormationProgressResponse() {
    }

    public FormationProgressResponse(List<FormationProgressContentItem> content,
                                      boolean examEligible, double completionPercentage) {
        this.content = content;
        this.examEligible = examEligible;
        this.completionPercentage = completionPercentage;
        this.formationStatus = STATUS_IN_PROGRESS;
    }

    public FormationProgressResponse(List<FormationProgressContentItem> content,
                                      boolean examEligible, double completionPercentage, String formationStatus) {
        this.content = content;
        this.examEligible = examEligible;
        this.completionPercentage = completionPercentage;
        this.formationStatus = formationStatus != null ? formationStatus : STATUS_IN_PROGRESS;
    }

    public List<FormationProgressContentItem> getContent() {
        return content;
    }

    public void setContent(List<FormationProgressContentItem> content) {
        this.content = content;
    }

    public boolean isExamEligible() {
        return examEligible;
    }

    public void setExamEligible(boolean examEligible) {
        this.examEligible = examEligible;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public String getFormationStatus() {
        return formationStatus;
    }

    public void setFormationStatus(String formationStatus) {
        this.formationStatus = formationStatus;
    }
}
