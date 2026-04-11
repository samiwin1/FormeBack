package tn.esprit.mentorservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tn.esprit.mentorservice.dto.formation.LearnerProgressSnapshotDto;

import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
public class FormationProgressClient {

    private final boolean enabled;
    private final String internalToken;
    private final WebClient webClient;

    public FormationProgressClient(
            @Value("${mentor.formation.enabled:true}") boolean enabled,
            @Value("${mentor.formation.internal-token:}") String internalToken,
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder loadBalancedBuilder
    ) {
        this.enabled = enabled;
        this.internalToken = internalToken;
        this.webClient = loadBalancedBuilder.build();
    }

    public Optional<LearnerProgressSnapshotDto> fetchLearnerProgress(long userId, Long formationId) {
        if (!enabled) {
            return Optional.empty();
        }
        try {
            WebClient.RequestHeadersSpec<?> spec = webClient.get()
                    .uri(uriBuilder -> {
                        var b = uriBuilder
                                .scheme("http")
                                .host("formation-service")
                                .path("/api/internal/mentor/learner-progress/{userId}");
                        if (formationId != null) {
                            b = b.queryParam("formationId", formationId);
                        }
                        return b.build(userId);
                    })
                    .header(HttpHeaders.ACCEPT, "application/json");

            if (internalToken != null && !internalToken.isBlank()) {
                spec = spec.header("X-Internal-Token", internalToken);
            }

            LearnerProgressSnapshotDto body = spec.retrieve()
                    .bodyToMono(LearnerProgressSnapshotDto.class)
                    .block(Duration.ofSeconds(15));

            return Optional.ofNullable(body);
        } catch (WebClientResponseException e) {
            log.warn("Formation progress fetch failed: {} {}", e.getStatusCode(), e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Formation progress fetch failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
