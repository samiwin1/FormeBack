package tn.esprit.formation_service.dto;

/**
 * Represents an incorrect quiz answer for AI explanation.
 * aiExplanation is populated by GeminiApiService.explainMistakes.
 */
public class IncorrectAnswerItem {

    private String questionText;
    private String correctAnswer;
    private String userAnswer;
    private String aiExplanation;

    public IncorrectAnswerItem() {
    }

    public IncorrectAnswerItem(String questionText, String correctAnswer, String userAnswer) {
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
        this.userAnswer = userAnswer;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public String getAiExplanation() {
        return aiExplanation;
    }

    public void setAiExplanation(String aiExplanation) {
        this.aiExplanation = aiExplanation;
    }
}
