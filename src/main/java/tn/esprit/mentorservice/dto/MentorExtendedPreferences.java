package tn.esprit.mentorservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.mentorservice.domain.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Optional extended learner preferences stored as JSON on {@code learner_portfolio}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MentorExtendedPreferences {

    @Valid
    @Size(max = 30)
    @Builder.Default
    private List<TargetCertificationDto> targetCertifications = new ArrayList<>();

    @Valid
    @Size(max = 30)
    @Builder.Default
    private List<HeldCertificationDto> heldCertifications = new ArrayList<>();

    private ExamFormatPreference examFormatPreference;
    private ExamAttemptContext examAttemptContext;
    private StudyModeForExams studyModeForExams;
    private MockExamFrequency mockExamFrequency;

    @Size(max = 64)
    private String timezone;

    @Size(max = 8)
    @Builder.Default
    private List<StudyWindow> studyWindows = new ArrayList<>();

    private DevicePrimary devicePrimary;

    @Size(max = 2000)
    private String accessibilityNotes;

    @Min(5)
    @Max(480)
    private Integer maxSessionMinutes;

    private PrimaryGoal primaryGoal;
    private ReminderStyle reminderStyle;
    private StreakSensitivity streakSensitivity;

    private DepthVsBreadth depthVsBreadth;
    private ExplanationStyle explanationStyle;

    @Size(max = 500)
    private String resourceLanguageTolerance;

    @Size(max = 32)
    @Builder.Default
    private List<String> preferredResourceLocales = new ArrayList<>();

    @Size(max = 50)
    @Builder.Default
    private List<Long> linkedFormationIds = new ArrayList<>();
}
