package tn.esprit.formation_service.dto;

import java.util.Map;

public class EvaluationSubmitRequest {

    private Long userId;
    /** Question index (as string key) -> selected option index. E.g. {"0": 1, "1": 0} */
    private Map<String, Integer> answers;

    public EvaluationSubmitRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Map<String, Integer> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, Integer> answers) {
        this.answers = answers;
    }
}
