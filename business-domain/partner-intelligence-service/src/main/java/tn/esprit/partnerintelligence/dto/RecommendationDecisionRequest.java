package tn.esprit.partnerintelligence.dto;
import jakarta.validation.constraints.NotBlank;public record RecommendationDecisionRequest(@NotBlank String decision,String comment,String reviewer){}
