package tn.esprit.shop.shopservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Client to call the formation-service (external microservice).
 * Uses Eureka service name "formation-service" for discovery.
 */
@Component
public class FormationClient {

    private static final String FORMATION_SERVICE_URL = "http://formation-service/api/formations";

    private final RestTemplate restTemplate;

    public FormationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches formation by id from formation-service.
     * @return FormationResponse or null if not found / service unavailable
     */
    public FormationResponse getFormationById(Long id) {
        try {
            return restTemplate.getForObject(FORMATION_SERVICE_URL + "/" + id, FormationResponse.class);
        } catch (Exception e) {
            return null;
        }
    }
}
