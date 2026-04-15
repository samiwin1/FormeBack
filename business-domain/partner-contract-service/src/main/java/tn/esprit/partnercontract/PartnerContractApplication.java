package tn.esprit.partnercontract;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PartnerContractApplication {
    public static void main(String[] args) {
        SpringApplication.run(PartnerContractApplication.class, args);
    }
}