package tn.esprit.voucherfraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import tn.esprit.voucherfraud.entities.FraudAlert;
import tn.esprit.voucherfraud.repositories.FraudAlertRepository;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableDiscoveryClient
public class VoucherFraudApplication {
    public static void main(String[] args) {
        SpringApplication.run(VoucherFraudApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadData(FraudAlertRepository fraudAlertRepository) {
        return args -> {
            if (fraudAlertRepository.count() == 0) {
                FraudAlert alert1 = new FraudAlert();
                alert1.setVoucherCode("VCH-ALRT-88A");
                alert1.setPartnerId(104L);
                alert1.setAlertType("MULTIPLE_REDEMPTIONS");
                alert1.setSeverityLevel(4);
                alert1.setDetectionDetails("Voucher redeemed 5 times within 10 minutes from the same IP address.");
                alert1.setStatus("INVESTIGATION_PENDING");
                alert1.setDetectedAt(LocalDateTime.now().minusHours(1));
                
                FraudAlert alert2 = new FraudAlert();
                alert2.setVoucherCode("VCH-ALRT-92B");
                alert2.setPartnerId(210L);
                alert2.setAlertType("GEOLOCATION_MISMATCH");
                alert2.setSeverityLevel(3);
                alert2.setDetectionDetails("Voucher redeemed in unexpected remote location, mismatching partner origin.");
                alert2.setStatus("INVESTIGATION_PENDING");
                alert2.setDetectedAt(LocalDateTime.now().minusDays(3));
                
                FraudAlert alert3 = new FraudAlert();
                alert3.setVoucherCode("VCH-ALRT-12C");
                alert3.setPartnerId(185L);
                alert3.setAlertType("UNUSUAL_VELOCITY");
                alert3.setSeverityLevel(5);
                alert3.setDetectionDetails("Redemption velocity exceeded safe thresholds by 400% during off-peak hours.");
                alert3.setStatus("INVESTIGATION_PENDING");
                alert3.setDetectedAt(LocalDateTime.now().minusDays(1));

                fraudAlertRepository.save(alert1);
                fraudAlertRepository.save(alert2);
                fraudAlertRepository.save(alert3);
                
                System.out.println("Seeded Voucher Fraud database with sample alerts.");
            }
        };
    }
}