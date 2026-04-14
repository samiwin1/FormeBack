package com.example.config;

import com.example.entity.AccessCode;
import com.example.entity.Deal;
import com.example.entity.Pack;
import com.example.entity.Partner;
import com.example.repository.AccessCodeRepository;
import com.example.repository.DealRepository;
import com.example.repository.PackRepository;
import com.example.repository.PartnerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DemoDataSeeder {

    @Bean
    @ConditionalOnProperty(name = "app.seed.demo-data", havingValue = "true", matchIfMissing = true)
    CommandLineRunner seedBusinessDemoData(
            PartnerRepository partnerRepository,
            DealRepository dealRepository,
            PackRepository packRepository,
            AccessCodeRepository accessCodeRepository
    ) {
        return args -> {
            boolean hasAnyData = partnerRepository.count() > 0
                    || dealRepository.count() > 0
                    || packRepository.count() > 0
                    || accessCodeRepository.count() > 0;

            if (hasAnyData) {
                return;
            }

            Partner alpha = partnerRepository.save(new Partner(
                    "Alpha Academy Tunisia",
                    "contact@alpha-academy.tn",
                    "+216 20 100 100"
            ));
            Partner beta = partnerRepository.save(new Partner(
                    "Beta Talent Hub",
                    "hello@beta-hub.tn",
                    "+216 21 200 200"
            ));
            Partner gamma = partnerRepository.save(new Partner(
                    "Gamma Skills",
                    "team@gamma-skills.tn",
                    "+216 22 300 300"
            ));
            Partner delta = partnerRepository.save(new Partner(
                    "Delta Tech Institute",
                    "partnerships@delta-tech.tn",
                    "+216 23 440 440"
            ));

            LocalDate today = LocalDate.now();

            List<Deal> seededDeals = dealRepository.saveAll(List.of(
                    new Deal(
                            "Java Certification Spring Drive",
                            "30% discount for backend learners preparing for certification.",
                            alpha.getId(),
                            today.minusDays(45),
                            today.plusDays(25)
                    ),
                    new Deal(
                            "Angular Professional Bundle",
                            "Special bundle for frontend track with guided mentoring.",
                            beta.getId(),
                            today.minusDays(20),
                            today.plusDays(40)
                    ),
                    new Deal(
                            "Corporate Upskilling Plan",
                            "Annual enterprise deal for large learner cohorts.",
                            gamma.getId(),
                            today.minusDays(70),
                            today.plusDays(120)
                    ),
                    new Deal(
                            "Evaluation Booster",
                            "Extra oral prep sessions and retake support.",
                            beta.getId(),
                            today.minusDays(14),
                            today.plusDays(16)
                    ),
                    new Deal(
                            "Graduate Fast Track",
                            "Intensive 8-week program with accelerated assessments.",
                            delta.getId(),
                            today.minusDays(5),
                            today.plusDays(55)
                    )
            ));

            packRepository.saveAll(List.of(
                                        new Pack("Starter Pack", "Core beginner formations and basic assessment access", 3, true),
                                        new Pack("Growth Pack", "Intermediate tracks, evaluator feedback, and progress analytics", 6, true),
                                        new Pack("Expert Pack", "Advanced tracks and certification preparation", 12, true),
                                        new Pack("Enterprise Pack", "Bulk seats for teams with central reporting", 12, true),
                                        new Pack("Legacy Pack", "Previous campaign pack retained for history", 2, false)
            ));

                        int year = Year.now().getValue();
                        List<AccessCode> codes = new ArrayList<>();
                        int seq = 1;

                        for (Deal deal : seededDeals) {
                                Long partnerId = deal.getPartnerId();
                                String prefix;
                                if (partnerId.equals(alpha.getId())) {
                                        prefix = "ALPHA";
                                } else if (partnerId.equals(beta.getId())) {
                                        prefix = "BETA";
                                } else if (partnerId.equals(gamma.getId())) {
                                        prefix = "GAMMA";
                                } else {
                                        prefix = "DELTA";
                                }

                                // One active code
                                codes.add(new AccessCode(
                                                String.format("%s-%d-%03d", prefix, year, seq++),
                                                partnerId,
                                                deal.getId(),
                                                today.plusDays(45),
                                                false
                                ));

                                // One used code
                                codes.add(new AccessCode(
                                                String.format("%s-%d-%03d", prefix, year, seq++),
                                                partnerId,
                                                deal.getId(),
                                                today.plusDays(20),
                                                true
                                ));

                                // One expired and unused code
                                codes.add(new AccessCode(
                                                String.format("%s-%d-%03d", prefix, year, seq++),
                                                partnerId,
                                                deal.getId(),
                                                today.minusDays(6),
                                                false
                                ));
                        }

                        accessCodeRepository.saveAll(codes);
        };
    }
}
