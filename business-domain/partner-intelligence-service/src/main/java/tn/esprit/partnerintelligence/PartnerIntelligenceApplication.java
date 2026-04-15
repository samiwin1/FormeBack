package tn.esprit.partnerintelligence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PartnerIntelligenceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PartnerIntelligenceApplication.class, args);
    }
}
