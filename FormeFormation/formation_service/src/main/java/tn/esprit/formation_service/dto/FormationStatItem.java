package tn.esprit.formation_service.dto;

public class FormationStatItem {

    private Long formationId;
    private String formationTitle;
    private long count;
    private double rate;

    public FormationStatItem() {
    }

    public FormationStatItem(Long formationId, String formationTitle, long count, double rate) {
        this.formationId = formationId;
        this.formationTitle = formationTitle;
        this.count = count;
        this.rate = rate;
    }

    public Long getFormationId() {
        return formationId;
    }

    public void setFormationId(Long formationId) {
        this.formationId = formationId;
    }

    public String getFormationTitle() {
        return formationTitle;
    }

    public void setFormationTitle(String formationTitle) {
        this.formationTitle = formationTitle;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
