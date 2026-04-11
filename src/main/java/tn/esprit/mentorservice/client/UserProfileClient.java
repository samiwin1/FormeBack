package tn.esprit.mentorservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class UserProfileClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${mentor.user-service.url:http://user-service}")
    private String userServiceUrl;

    public UserProfileClient(@Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * Returns a map with "email" and "firstName" for the given userId.
     * Empty on any failure so the scheduler can skip gracefully.
     */
    public Optional<Map<String, String>> fetchUserInfo(long userId) {
        try {
            Map<?, ?> resp = webClientBuilder.build()
                    .get()
                    .uri(userServiceUrl + "/users/" + userId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            if (resp == null) return Optional.empty();
            String email     = (String) resp.get("email");
            String firstName = (String) resp.get("firstName");
            String lastName  = (String) resp.get("lastName");
            if (email == null) return Optional.empty();
            return Optional.of(Map.of(
                    "email",     email,
                    "firstName", firstName != null ? firstName : email,
                    "lastName",  lastName  != null ? lastName  : ""
            ));
        } catch (Exception e) {
            log.warn("Could not resolve user info for userId={}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }
}
