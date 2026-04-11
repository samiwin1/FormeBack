package tn.esprit.mentorservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.mentorservice.common.ApiException;
import tn.esprit.mentorservice.dto.PortfolioResponseDto;
import tn.esprit.mentorservice.dto.PortfolioUpsertRequest;
import tn.esprit.mentorservice.dto.SkillResponseDto;
import tn.esprit.mentorservice.dto.SkillUpsertDto;
import tn.esprit.mentorservice.entity.LearnerPortfolio;
import tn.esprit.mentorservice.entity.LearnerSkill;
import tn.esprit.mentorservice.repository.LearnerPortfolioRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final LearnerPortfolioRepository portfolioRepository;

    @Transactional(readOnly = true)
    public boolean portfolioExists(long userId) {
        return portfolioRepository.findByUserId(userId).isPresent();
    }

    @Transactional(readOnly = true)
    public PortfolioResponseDto get(long userId) {
        LearnerPortfolio p = portfolioRepository.findWithSkillsByUserId(userId)
                .orElseThrow(() -> new ApiException(404, "Portfolio not found. Create one with PUT /mentor/portfolio."));
        return toDto(p);
    }

    @Transactional
    public PortfolioResponseDto upsert(long userId, PortfolioUpsertRequest req) {
        LearnerPortfolio p = portfolioRepository.findWithSkillsByUserId(userId)
                .orElseGet(() -> LearnerPortfolio.builder().userId(userId).build());

        p.setCurrentRole(req.currentRole());
        p.setTargetRole(req.targetRole());
        p.setExperienceLevel(req.experienceLevel());
        p.setBio(req.bio());
        p.setWeeklyStudyHours(req.weeklyStudyHours());
        if (req.preferredLanguage() != null && !req.preferredLanguage().isBlank()) {
            p.setPreferredLanguage(req.preferredLanguage().trim().toLowerCase());
        } else {
            p.setPreferredLanguage(null);
        }
        p.setLearningStyle(req.learningStyle());
        p.setExtendedPreferences(req.extendedPreferences());

        p.getSkills().clear();
        if (req.skills() != null) {
            for (SkillUpsertDto s : req.skills()) {
                LearnerSkill ls = LearnerSkill.builder()
                        .portfolio(p)
                        .skillName(s.skillName().trim())
                        .skillLevel(s.level())
                        .build();
                p.getSkills().add(ls);
            }
        }

        p = portfolioRepository.save(p);
        return toDto(portfolioRepository.findWithSkillsByUserId(p.getUserId()).orElse(p));
    }

    @Transactional(readOnly = true)
    public LearnerPortfolio requirePortfolio(long userId) {
        return portfolioRepository.findWithSkillsByUserId(userId)
                .orElseThrow(() -> new ApiException(400, "Complete your learning profile first (PUT /mentor/portfolio)."));
    }

    private static PortfolioResponseDto toDto(LearnerPortfolio p) {
        List<SkillResponseDto> skills = p.getSkills().stream()
                .map(s -> new SkillResponseDto(s.getSkillName(), s.getSkillLevel()))
                .collect(Collectors.toList());
        return new PortfolioResponseDto(
                p.getId(),
                p.getUserId(),
                p.getCurrentRole(),
                p.getTargetRole(),
                p.getExperienceLevel(),
                p.getBio(),
                p.getWeeklyStudyHours(),
                p.getPreferredLanguage(),
                p.getLearningStyle(),
                p.getExtendedPreferences(),
                skills,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
