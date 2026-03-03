package tn.esprit.formation_service.dto;

public class GlobalAnalyticsResponse {

    private TrainingCompletionStats trainingCompletion;
    private AssessmentSuccessStats assessmentSuccess;
    private ExamAnalysisStats examAnalysis;
    private LearningObstacleStats learningObstacles;
    /** AI-generated insights (optional, populated when /insights endpoint is used) */
    private String aiInsights;

    public GlobalAnalyticsResponse() {
    }

    public GlobalAnalyticsResponse(TrainingCompletionStats trainingCompletion,
                                  AssessmentSuccessStats assessmentSuccess,
                                  ExamAnalysisStats examAnalysis,
                                  LearningObstacleStats learningObstacles) {
        this.trainingCompletion = trainingCompletion;
        this.assessmentSuccess = assessmentSuccess;
        this.examAnalysis = examAnalysis;
        this.learningObstacles = learningObstacles;
    }

    public TrainingCompletionStats getTrainingCompletion() {
        return trainingCompletion;
    }

    public void setTrainingCompletion(TrainingCompletionStats trainingCompletion) {
        this.trainingCompletion = trainingCompletion;
    }

    public AssessmentSuccessStats getAssessmentSuccess() {
        return assessmentSuccess;
    }

    public void setAssessmentSuccess(AssessmentSuccessStats assessmentSuccess) {
        this.assessmentSuccess = assessmentSuccess;
    }

    public ExamAnalysisStats getExamAnalysis() {
        return examAnalysis;
    }

    public void setExamAnalysis(ExamAnalysisStats examAnalysis) {
        this.examAnalysis = examAnalysis;
    }

    public LearningObstacleStats getLearningObstacles() {
        return learningObstacles;
    }

    public void setLearningObstacles(LearningObstacleStats learningObstacles) {
        this.learningObstacles = learningObstacles;
    }

    public String getAiInsights() {
        return aiInsights;
    }

    public void setAiInsights(String aiInsights) {
        this.aiInsights = aiInsights;
    }
}
