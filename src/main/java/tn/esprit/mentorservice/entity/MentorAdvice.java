package tn.esprit.mentorservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mentor_advice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorAdvice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private MentorSession session;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "structured_json", columnDefinition = "LONGTEXT")
    private String structuredJson;

    @Column(name = "raw_text", columnDefinition = "LONGTEXT")
    private String rawText;
}
