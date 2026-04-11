package tn.esprit.mentorservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.mentorservice.common.ApiException;
import tn.esprit.mentorservice.dto.demand.FormationDemandAggregateDto;
import tn.esprit.mentorservice.dto.demand.FormationDemandDto;
import tn.esprit.mentorservice.dto.demand.RoleDemandCountDto;
import tn.esprit.mentorservice.entity.FormationDemand;
import tn.esprit.mentorservice.repository.FormationDemandRepository;
import tn.esprit.mentorservice.service.MentorNotificationService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mentor/admin/formation-demands")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminFormationDemandController {

    private final FormationDemandRepository formationDemandRepository;
    private final MentorNotificationService mentorNotificationService;

    /** Aggregated view: one entry per role with recent individual demands. */
    @GetMapping
    public List<FormationDemandAggregateDto> getDemands() {
        List<FormationDemand> all = formationDemandRepository.findAllByOrderByCreatedAtDesc();

        // Group by role
        Map<String, List<FormationDemandDto>> grouped = new LinkedHashMap<>();
        for (FormationDemand d : all) {
            String role = d.getRequestedRole() != null ? d.getRequestedRole() : "Unknown";
            grouped.computeIfAbsent(role, k -> new ArrayList<>())
                   .add(toDto(d));
        }

        List<FormationDemandAggregateDto> result = new ArrayList<>();
        grouped.forEach((role, demands) ->
            result.add(new FormationDemandAggregateDto(role, demands.size(), demands))
        );
        // Sort by most requests first
        result.sort((a, b) -> Long.compare(b.totalRequests(), a.totalRequests()));
        return result;
    }

    /** Count per role — used for charts. */
    @GetMapping("/by-role")
    public List<RoleDemandCountDto> getByRole() {
        return formationDemandRepository.countByRole()
                .stream()
                .map(row -> new RoleDemandCountDto((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    /** Mark a demand as fulfilled and notify the learner who requested it. */
    @PostMapping("/{demandId}/fulfill")
    public ResponseEntity<Void> fulfill(@PathVariable Long demandId) {
        FormationDemand demand = formationDemandRepository.findById(demandId)
                .orElseThrow(() -> new ApiException(404, "Formation demand not found"));
        if (!demand.isFulfilled()) {
            demand.setFulfilled(true);
            formationDemandRepository.save(demand);
            mentorNotificationService.notifyUserFormationFulfilled(
                    demand.getUserId(), demand.getId(), demand.getRequestedRole());
        }
        return ResponseEntity.noContent().build();
    }

    private static FormationDemandDto toDto(FormationDemand d) {
        return new FormationDemandDto(
                d.getId(),
                d.getUserId(),
                d.getRequestedRole(),
                d.getDetectedSkillsGap(),
                d.getCreatedAt()
        );
    }
}
