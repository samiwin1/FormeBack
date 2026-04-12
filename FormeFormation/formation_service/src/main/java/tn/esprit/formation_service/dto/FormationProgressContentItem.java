package tn.esprit.formation_service.dto;

public class FormationProgressContentItem {

    private Long id;
    private String title;
    private Integer orderIndex;
    private boolean unlocked;
    private boolean evaluationPassed;
    private Long evaluationId;

    public FormationProgressContentItem() {
    }

    public FormationProgressContentItem(Long id, String title, Integer orderIndex,
                                        boolean unlocked, boolean evaluationPassed, Long evaluationId) {
        this.id = id;
        this.title = title;
        this.orderIndex = orderIndex;
        this.unlocked = unlocked;
        this.evaluationPassed = evaluationPassed;
        this.evaluationId = evaluationId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public boolean isEvaluationPassed() {
        return evaluationPassed;
    }

    public void setEvaluationPassed(boolean evaluationPassed) {
        this.evaluationPassed = evaluationPassed;
    }

    public Long getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(Long evaluationId) {
        this.evaluationId = evaluationId;
    }
}
