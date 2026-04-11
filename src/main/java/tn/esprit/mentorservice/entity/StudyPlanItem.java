package tn.esprit.mentorservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tn.esprit.mentorservice.domain.StudyPlanItemType;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_plan_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyPlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_plan_id", nullable = false)
    private StudyPlan studyPlan;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "item_type", nullable = false, length = 32)
    private StudyPlanItemType itemType;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "resource_ref", length = 500)
    private String resourceRef;

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
