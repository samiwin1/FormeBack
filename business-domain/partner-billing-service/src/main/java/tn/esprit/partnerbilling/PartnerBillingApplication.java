package tn.esprit.partnerbilling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PartnerBillingApplication {
    public static void main(String[] args) {
        SpringApplication.run(PartnerBillingApplication.class, args);
    }
}