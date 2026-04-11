package tn.esprit.mentorservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tn.esprit.mentorservice.dto.formation.FormationCatalogItemDto;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class FormationCatalogClient {

    private final boolean enabled;
    private final String internalToken;
    private final WebClient webClient;

    public FormationCatalogClient(
            @Value("${mentor.formation.enabled:true}") boolean enabled,
            @Value("${mentor.formation.internal-token:}") String internalToken,
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder loadBalancedBuilder
    ) {
        this.enabled = enabled;
        this.internalToken = internalToken;
        this.webClient = loadBalancedBuilder.build();
    }

    public List<FormationCatalogItemDto> fetchPublishedCatalog() {
        if (!enabled) {
            return Collections.emptyList();
        }
        try {
            WebClient.RequestHeadersSpec<?> spec = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("formation-service")
                            .path("/api/internal/mentor/formations-catalog")
                            .build())
                    .header(HttpHeaders.ACCEPT, "application/json");

            if (internalToken != null && !internalToken.isBlank()) {
                spec = spec.header("X-Internal-Token", internalToken);
            }

            List<FormationCatalogItemDto> list = spec.retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<FormationCatalogItemDto>>() {})
                    .block(Duration.ofSeconds(15));
            return list != null ? list : Collections.emptyList();
        } catch (WebClientResponseException e) {
            log.warn("Formation catalog fetch failed: {} {}", e.getStatusCode(), e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Formation catalog fetch failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
