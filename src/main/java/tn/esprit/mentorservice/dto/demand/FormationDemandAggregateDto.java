package tn.esprit.mentorservice.dto.demand;

import java.util.List;

public record FormationDemandAggregateDto(
        String requestedRole,
        long totalRequests,
        List<FormationDemandDto> recentDemands
) {}
