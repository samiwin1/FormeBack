package tn.esprit.formation_service.dto;

import java.util.List;

public class FormationProgressResponse {

    private List<FormationProgressContentItem> content;
    private boolean examEligible;
    private double completionPercentage;

    public FormationProgressResponse() {
    }

    public FormationProgressResponse(List<FormationProgressContentItem> content,
                                      boolean examEligible, double completionPercentage) {
        this.content = content;
        this.examEligible = examEligible;
        this.completionPercentage = completionPercentage;
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
}
