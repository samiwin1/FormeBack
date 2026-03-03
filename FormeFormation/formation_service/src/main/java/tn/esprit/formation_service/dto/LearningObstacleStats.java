package tn.esprit.formation_service.dto;

import java.util.List;

public class LearningObstacleStats {

    private List<ObstacleItem> evaluationObstacles;
    private List<ObstacleItem> retryObstacles;
    private List<DropOffObstacleItem> dropOffObstacles;

    public LearningObstacleStats() {
    }

    public LearningObstacleStats(List<ObstacleItem> evaluationObstacles,
                                List<ObstacleItem> retryObstacles,
                                List<DropOffObstacleItem> dropOffObstacles) {
        this.evaluationObstacles = evaluationObstacles;
        this.retryObstacles = retryObstacles;
        this.dropOffObstacles = dropOffObstacles;
    }

    public List<ObstacleItem> getEvaluationObstacles() {
        return evaluationObstacles;
    }

    public void setEvaluationObstacles(List<ObstacleItem> evaluationObstacles) {
        this.evaluationObstacles = evaluationObstacles;
    }

    public List<ObstacleItem> getRetryObstacles() {
        return retryObstacles;
    }

    public void setRetryObstacles(List<ObstacleItem> retryObstacles) {
        this.retryObstacles = retryObstacles;
    }

    public List<DropOffObstacleItem> getDropOffObstacles() {
        return dropOffObstacles;
    }

    public void setDropOffObstacles(List<DropOffObstacleItem> dropOffObstacles) {
        this.dropOffObstacles = dropOffObstacles;
    }

    public static class ObstacleItem {
        private Long evaluationId;
        private String evaluationTitle;
        private String formationTitle;
        private double indicatorValue;

        public ObstacleItem() {
        }

        public ObstacleItem(Long evaluationId, String evaluationTitle, String formationTitle, double indicatorValue) {
            this.evaluationId = evaluationId;
            this.evaluationTitle = evaluationTitle;
            this.formationTitle = formationTitle;
            this.indicatorValue = indicatorValue;
        }

        public Long getEvaluationId() { return evaluationId; }
        public void setEvaluationId(Long evaluationId) { this.evaluationId = evaluationId; }
        public String getEvaluationTitle() { return evaluationTitle; }
        public void setEvaluationTitle(String evaluationTitle) { this.evaluationTitle = evaluationTitle; }
        public String getFormationTitle() { return formationTitle; }
        public void setFormationTitle(String formationTitle) { this.formationTitle = formationTitle; }
        public double getIndicatorValue() { return indicatorValue; }
        public void setIndicatorValue(double indicatorValue) { this.indicatorValue = indicatorValue; }
    }

    public static class DropOffObstacleItem {
        private Long formationId;
        private String formationTitle;
        private long userCount;

        public DropOffObstacleItem() {
        }

        public DropOffObstacleItem(Long formationId, String formationTitle, long userCount) {
            this.formationId = formationId;
            this.formationTitle = formationTitle;
            this.userCount = userCount;
        }

        public Long getFormationId() { return formationId; }
        public void setFormationId(Long formationId) { this.formationId = formationId; }
        public String getFormationTitle() { return formationTitle; }
        public void setFormationTitle(String formationTitle) { this.formationTitle = formationTitle; }
        public long getUserCount() { return userCount; }
        public void setUserCount(long userCount) { this.userCount = userCount; }
    }
}
