package tn.esprit.forme.certificationservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import tn.esprit.forme.certificationservice.infrastructure.feign.UserClient;

@Component("professionGuard")
@RequestScope
@RequiredArgsConstructor

public class ProfessionGuard {

    private final UserClient userClient;

    private Long cachedUserId;
    private String cachedProfession;

    public boolean isEvaluator() {
        return "EVALUATOR".equalsIgnoreCase(currentProfession());
    }

    public boolean isLearner() {
        String profession = currentProfession();
        return "LEARNER".equalsIgnoreCase(profession)
                || "STUDENT".equalsIgnoreCase(profession);
    }

    private String currentProfession() {
        Long userId = SecurityUtils.currentUserId();
        if (cachedUserId != null && cachedUserId.equals(userId) && cachedProfession != null) {
            return cachedProfession;
        }

        var user = userClient.getMe();
        cachedUserId = user.getId() == null ? userId : user.getId();
        cachedProfession = user.getProfession();
        return cachedProfession == null ? "" : cachedProfession;
    }
}
