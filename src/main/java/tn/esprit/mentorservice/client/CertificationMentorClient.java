package tn.esprit.mentorservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tn.esprit.mentorservice.dto.certification.MentorLearnerContextDto;

import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
public class CertificationMentorClient {

    private final boolean enabled;
    private final String internalToken;
    private final WebClient webClient;

    public CertificationMentorClient(
            @Value("${mentor.certification.enabled:true}") boolean enabled,
            @Value("${mentor.certification.internal-token:}") String internalToken,
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder loadBalancedBuilder
    ) {
        this.enabled = enabled;
        this.internalToken = internalToken;
        this.webClient = loadBalancedBuilder.build();
    }

    public Optional<MentorLearnerContextDto> fetchLearnerContext(long userId) {
        if (!enabled) {
            return Optional.empty();
        }
        try {
            WebClient.RequestHeadersSpec<?> spec = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("certification-service")
                            .path("/api/internal/mentor/learner-context/{userId}")
                            .build(userId))
                    .header(HttpHeaders.ACCEPT, "application/json");

            if (internalToken != null && !internalToken.isBlank()) {
                spec = spec.header("X-Internal-Token", internalToken);
            }

            MentorLearnerContextDto body = spec.retrieve()
                    .bodyToMono(MentorLearnerContextDto.class)
                    .block(Duration.ofSeconds(20));

            return Optional.ofNullable(body);
        } catch (WebClientResponseException e) {
            log.warn("Certification mentor context fetch failed: {} {}", e.getStatusCode(), e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Certification mentor context fetch failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
