package tn.esprit.events_service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.events_service.entity.EventTier;

/**
 * Placeholder for email notifications. User-service has no mail module yet;
 * wire SMTP or a notification microservice here later.
 */
@Service
public class EventEmailService {

    private static final Logger log = LoggerFactory.getLogger(EventEmailService.class);

    public void sendPartnerTierConfirmation(Long partnerId, String partnerEmail, String eventTitle, EventTier tier) {
        log.info(
                "[EVENT EMAIL PLACEHOLDER] partnerId={} email={} — Confirmed {} sponsorship for \"{}\". (Integrate real mail later.)",
                partnerId,
                partnerEmail != null ? partnerEmail : "n/a",
                tier,
                eventTitle
        );
    }

    public void sendTierFullNotice(Long partnerId, String partnerEmail, String eventTitle, EventTier tier) {
        log.info(
                "[EVENT EMAIL PLACEHOLDER] partnerId={} — Tier {} is full for event \"{}\". {}",
                partnerId,
                tier,
                eventTitle,
                partnerEmail != null ? "email=" + partnerEmail : ""
        );
    }
}
