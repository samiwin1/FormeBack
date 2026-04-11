package tn.esprit.mentorservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tn.esprit.mentorservice.converter.MentorExtendedPreferencesJsonConverter;
import tn.esprit.mentorservice.domain.ExperienceLevel;
import tn.esprit.mentorservice.domain.LearningStyle;
import tn.esprit.mentorservice.dto.MentorExtendedPreferences;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learner_portfolio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearnerPortfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "present_role", length = 100)
    private String currentRole;

    @Column(name = "target_role", length = 100)
    private String targetRole;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "experience_level", nullable = false, length = 32)
    private ExperienceLevel experienceLevel;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "weekly_study_hours")
    private Integer weeklyStudyHours;

    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "learning_style", nullable = false, length = 32)
    private LearningStyle learningStyle;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LearnerSkill> skills = new ArrayList<>();

    @Convert(converter = MentorExtendedPreferencesJsonConverter.class)
    @Column(name = "extended_preferences", columnDefinition = "LONGTEXT")
    private MentorExtendedPreferences extendedPreferences;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
