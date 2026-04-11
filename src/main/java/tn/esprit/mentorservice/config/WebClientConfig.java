package tn.esprit.mentorservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /** Plain WebClient for external hosts (Google Generative Language API). */
    @Bean(name = "vanillaWebClientBuilder")
    public WebClient.Builder vanillaWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean(name = "loadBalancedWebClientBuilder")
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}
