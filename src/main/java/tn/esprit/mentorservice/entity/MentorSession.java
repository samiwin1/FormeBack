package tn.esprit.mentorservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tn.esprit.mentorservice.domain.MentorChannel;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "mentor_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 32)
    private MentorChannel channel;

    @Column(name = "formation_id")
    private Long formationId;

    @Column(name = "model_used", length = 100)
    private String modelUsed;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
