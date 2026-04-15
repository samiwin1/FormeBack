package tn.esprit.events_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateEventRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String requirements;

    @NotBlank
    private String successMetrics;

    @NotNull
    private Instant startDate;

    @NotNull
    private Instant deadline;

    @NotNull
    @Min(0)
    private Integer maxVip;

    @NotNull
    @Min(0)
    private Integer maxGold;

    @NotNull
    @Min(0)
    private Integer maxSilver;

    @NotNull
    @Min(0)
    private Integer vipPrice;

    @NotNull
    @Min(0)
    private Integer goldPrice;

    @NotNull
    @Min(0)
    private Integer silverPrice;
}
