package tn.esprit.formation_service.dto;

import java.util.List;

public class TrainingCompletionStats {

    private long totalStarted;
    private long totalCompleted;
    private double completionRatePercent;
    private List<FormationStatItem> topCompletedFormations;
    private List<FormationStatItem> topAbandonedFormations;

    public TrainingCompletionStats() {
    }

    public TrainingCompletionStats(long totalStarted, long totalCompleted, double completionRatePercent,
                                   List<FormationStatItem> topCompletedFormations,
                                   List<FormationStatItem> topAbandonedFormations) {
        this.totalStarted = totalStarted;
        this.totalCompleted = totalCompleted;
        this.completionRatePercent = completionRatePercent;
        this.topCompletedFormations = topCompletedFormations;
        this.topAbandonedFormations = topAbandonedFormations;
    }

    public long getTotalStarted() {
        return totalStarted;
    }

    public void setTotalStarted(long totalStarted) {
        this.totalStarted = totalStarted;
    }

    public long getTotalCompleted() {
        return totalCompleted;
    }

    public void setTotalCompleted(long totalCompleted) {
        this.totalCompleted = totalCompleted;
    }

    public double getCompletionRatePercent() {
        return completionRatePercent;
    }

    public void setCompletionRatePercent(double completionRatePercent) {
        this.completionRatePercent = completionRatePercent;
    }

    public List<FormationStatItem> getTopCompletedFormations() {
        return topCompletedFormations;
    }

    public void setTopCompletedFormations(List<FormationStatItem> topCompletedFormations) {
        this.topCompletedFormations = topCompletedFormations;
    }

    public List<FormationStatItem> getTopAbandonedFormations() {
        return topAbandonedFormations;
    }

    public void setTopAbandonedFormations(List<FormationStatItem> topAbandonedFormations) {
        this.topAbandonedFormations = topAbandonedFormations;
    }
}
