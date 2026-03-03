package tn.esprit.formation_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for Gemini API integration.
 * API key: set gemini.api.key in application.properties (from https://aistudio.google.com/app/apikey)
 */
@Configuration
public class GeminiConfig {

    @Bean("geminiRestTemplate")
    public RestTemplate geminiRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);  // 10 seconds
        factory.setReadTimeout(30000);     // 30 seconds
        return new RestTemplate(factory);
    }
}
