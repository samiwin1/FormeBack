package tn.esprit.shop.shopservice.modules.formation.dto;

import tn.esprit.shop.shopservice.modules.formation.entity.FormationStatus;

public record FormationRequest(
        String title,
        String description,
        String category,
        String level,
        String objectives,
        String skillsTargeted,
        FormationStatus status,
        Long createdBy
) {}
