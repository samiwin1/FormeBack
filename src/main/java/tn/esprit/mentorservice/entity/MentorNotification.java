package tn.esprit.mentorservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tn.esprit.mentorservice.domain.MentorNotificationType;

import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** NULL = admin broadcast; non-null = targeted learner notification */
    @Column(name = "target_user_id")
    private Long targetUserId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 40)
    private MentorNotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    /** demandId for context / in-app navigation */
    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
