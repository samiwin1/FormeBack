package tn.esprit.formation_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
public class FormationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FormationServiceApplication.class, args);
    }

}
