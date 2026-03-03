package tn.esprit.formation_service.dto;

import java.util.Map;

public class SaveAnswerRequest {

    private Map<String, Integer> answers;

    public Map<String, Integer> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, Integer> answers) {
        this.answers = answers;
    }
}
