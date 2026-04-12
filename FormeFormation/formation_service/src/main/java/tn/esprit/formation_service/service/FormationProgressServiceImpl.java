package tn.esprit.formation_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.formation_service.dto.FormationProgressContentItem;
import tn.esprit.formation_service.dto.FormationProgressResponse;
import tn.esprit.formation_service.entity.ContenuFormation;
import tn.esprit.formation_service.entity.Evaluation;
import tn.esprit.formation_service.entity.ResultEvaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FormationProgressServiceImpl implements FormationProgressService {

    private final FormationService formationService;
    private final ContenuFormationService contenuFormationService;
    private final ResultEvaluationService resultEvaluationService;
    private final ValidationService validationService;

    public FormationProgressServiceImpl(FormationService formationService,
                                         ContenuFormationService contenuFormationService,
                                         ResultEvaluationService resultEvaluationService,
                                         ValidationService validationService) {
        this.formationService = formationService;
        this.contenuFormationService = contenuFormationService;
        this.resultEvaluationService = resultEvaluationService;
        this.validationService = validationService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FormationProgressResponse> getFormationProgress(Long userId, Long formationId) {
        if (formationService.findById(formationId).isEmpty()) {
            return Optional.empty();
        }

        List<ContenuFormation> blocks = contenuFormationService.findByFormationId(formationId);
        List<ResultEvaluation> userResults = userId != null
                ? resultEvaluationService.findByUser_id(userId)
                : List.of();

        Set<Long> passedEvaluationIds = userResults.stream()
                .filter(r -> Boolean.TRUE.equals(r.getPassed()))
                .map(r -> r.getEvaluation() != null ? r.getEvaluation().getId() : null)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        List<FormationProgressContentItem> content = new ArrayList<>();
        int completedStepsCount = 0;
        Set<Long> blockLinkedEvaluationIds = new java.util.HashSet<>();

        for (int i = 0; i < blocks.size(); i++) {
            ContenuFormation block = blocks.get(i);
            Evaluation thisBlockEval = block.getEvaluation();

            boolean unlocked;
            boolean evaluationPassed;

            // Block N is unlocked only when user has passed ALL evaluations from blocks 0..N-1
            boolean computedUnlocked = true;
            for (int j = 0; j < i; j++) {
                Evaluation eval = blocks.get(j).getEvaluation();
                if (eval != null && !passedEvaluationIds.contains(eval.getId())) {
                    computedUnlocked = false;
                    break;
                }
            }
            unlocked = computedUnlocked;

            evaluationPassed = thisBlockEval == null || passedEvaluationIds.contains(thisBlockEval.getId());

            if (thisBlockEval != null) {
                blockLinkedEvaluationIds.add(thisBlockEval.getId());
            }
            if (evaluationPassed) {
                completedStepsCount++;
            }
            if (thisBlockEval != null && passedEvaluationIds.contains(thisBlockEval.getId())) {
                completedStepsCount++;
            }

            Long evalId = thisBlockEval != null ? thisBlockEval.getId() : null;
            content.add(new FormationProgressContentItem(
                    block.getId(),
                    block.getTitle(),
                    block.getOrder_index(),
                    unlocked,
                    evaluationPassed,
                    evalId
            ));
        }

        boolean examEligible = blockLinkedEvaluationIds.isEmpty()
                || blockLinkedEvaluationIds.stream().allMatch(passedEvaluationIds::contains);

        int totalSteps = blocks.size() + (int) blocks.stream().filter(b -> b.getEvaluation() != null).count();
        double completionPercentage = totalSteps > 0
                ? Math.round(((double) completedStepsCount / totalSteps) * 100.0)
                : 0.0;

        String formationStatus = (userId != null && validationService.isFormationCompleted(userId, formationId))
                ? FormationProgressResponse.STATUS_COMPLETED
                : FormationProgressResponse.STATUS_IN_PROGRESS;

        return Optional.of(new FormationProgressResponse(content, examEligible, completionPercentage, formationStatus));
    }
}
