package tn.esprit.formation_service.dto;

import java.util.List;

public class ExamAnalysisStats {

    private double passRate;
    private double averageScore;
    private Double averageCompletionMinutes;
    private AbnormalExamPatterns abnormalPatterns;

    public ExamAnalysisStats() {
    }

    public ExamAnalysisStats(double passRate, double averageScore, Double averageCompletionMinutes,
                             AbnormalExamPatterns abnormalPatterns) {
        this.passRate = passRate;
        this.averageScore = averageScore;
        this.averageCompletionMinutes = averageCompletionMinutes;
        this.abnormalPatterns = abnormalPatterns;
    }

    public double getPassRate() {
        return passRate;
    }

    public void setPassRate(double passRate) {
        this.passRate = passRate;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public Double getAverageCompletionMinutes() {
        return averageCompletionMinutes;
    }

    public void setAverageCompletionMinutes(Double averageCompletionMinutes) {
        this.averageCompletionMinutes = averageCompletionMinutes;
    }

    public AbnormalExamPatterns getAbnormalPatterns() {
        return abnormalPatterns;
    }

    public void setAbnormalPatterns(AbnormalExamPatterns abnormalPatterns) {
        this.abnormalPatterns = abnormalPatterns;
    }

    public static class AbnormalExamPatterns {
        private List<RepeatedFailureItem> repeatedFailures;
        private List<LowScoreExamItem> lowScores;
        private List<LongDurationExamItem> longDurations;

        public AbnormalExamPatterns() {
        }

        public AbnormalExamPatterns(List<RepeatedFailureItem> repeatedFailures,
                                   List<LowScoreExamItem> lowScores,
                                   List<LongDurationExamItem> longDurations) {
            this.repeatedFailures = repeatedFailures;
            this.lowScores = lowScores;
            this.longDurations = longDurations;
        }

        public List<RepeatedFailureItem> getRepeatedFailures() {
            return repeatedFailures;
        }

        public void setRepeatedFailures(List<RepeatedFailureItem> repeatedFailures) {
            this.repeatedFailures = repeatedFailures;
        }

        public List<LowScoreExamItem> getLowScores() {
            return lowScores;
        }

        public void setLowScores(List<LowScoreExamItem> lowScores) {
            this.lowScores = lowScores;
        }

        public List<LongDurationExamItem> getLongDurations() {
            return longDurations;
        }

        public void setLongDurations(List<LongDurationExamItem> longDurations) {
            this.longDurations = longDurations;
        }
    }

    public static class RepeatedFailureItem {
        private Long examenId;
        private String examenTitle;
        private Long userId;
        private int failureCount;

        public RepeatedFailureItem() {
        }

        public RepeatedFailureItem(Long examenId, String examenTitle, Long userId, int failureCount) {
            this.examenId = examenId;
            this.examenTitle = examenTitle;
            this.userId = userId;
            this.failureCount = failureCount;
        }

        public Long getExamenId() { return examenId; }
        public void setExamenId(Long examenId) { this.examenId = examenId; }
        public String getExamenTitle() { return examenTitle; }
        public void setExamenTitle(String examenTitle) { this.examenTitle = examenTitle; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public int getFailureCount() { return failureCount; }
        public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
    }

    public static class LowScoreExamItem {
        private Long examenId;
        private String examenTitle;
        private String formationTitle;
        private double averageScore;

        public LowScoreExamItem() {
        }

        public LowScoreExamItem(Long examenId, String examenTitle, String formationTitle, double averageScore) {
            this.examenId = examenId;
            this.examenTitle = examenTitle;
            this.formationTitle = formationTitle;
            this.averageScore = averageScore;
        }

        public Long getExamenId() { return examenId; }
        public void setExamenId(Long examenId) { this.examenId = examenId; }
        public String getExamenTitle() { return examenTitle; }
        public void setExamenTitle(String examenTitle) { this.examenTitle = examenTitle; }
        public String getFormationTitle() { return formationTitle; }
        public void setFormationTitle(String formationTitle) { this.formationTitle = formationTitle; }
        public double getAverageScore() { return averageScore; }
        public void setAverageScore(double averageScore) { this.averageScore = averageScore; }
    }

    public static class LongDurationExamItem {
        private Long examenId;
        private String examenTitle;
        private String formationTitle;
        private double averageDurationMinutes;
        private int expectedDurationMinutes;

        public LongDurationExamItem() {
        }

        public LongDurationExamItem(Long examenId, String examenTitle, String formationTitle,
                                   double averageDurationMinutes, int expectedDurationMinutes) {
            this.examenId = examenId;
            this.examenTitle = examenTitle;
            this.formationTitle = formationTitle;
            this.averageDurationMinutes = averageDurationMinutes;
            this.expectedDurationMinutes = expectedDurationMinutes;
        }

        public Long getExamenId() { return examenId; }
        public void setExamenId(Long examenId) { this.examenId = examenId; }
        public String getExamenTitle() { return examenTitle; }
        public void setExamenTitle(String examenTitle) { this.examenTitle = examenTitle; }
        public String getFormationTitle() { return formationTitle; }
        public void setFormationTitle(String formationTitle) { this.formationTitle = formationTitle; }
        public double getAverageDurationMinutes() { return averageDurationMinutes; }
        public void setAverageDurationMinutes(double averageDurationMinutes) { this.averageDurationMinutes = averageDurationMinutes; }
        public int getExpectedDurationMinutes() { return expectedDurationMinutes; }
        public void setExpectedDurationMinutes(int expectedDurationMinutes) { this.expectedDurationMinutes = expectedDurationMinutes; }
    }
}
