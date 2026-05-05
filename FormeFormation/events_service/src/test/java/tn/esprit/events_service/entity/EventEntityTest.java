package tn.esprit.events_service.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Event entity unit tests")
class EventEntityTest {

    @Test
    @DisplayName("Should retain title after builder construction")
    void shouldRetainCoreFieldsViaBuilder() {
        Instant start = Instant.parse("2026-06-01T08:00:00Z");
        Instant deadline = Instant.parse("2026-12-01T17:00:00Z");

        Event event = Event.builder()
                .title("Hackathon TN")
                .description("Quarterly sprint")
                .requirements("Laptop")
                .successMetrics("Deployed demo")
                .startDate(start)
                .deadline(deadline)
                .maxVip(2)
                .maxGold(4)
                .maxSilver(8)
                .vipPrice(100)
                .goldPrice(50)
                .silverPrice(25)
                .build();

        assertNotNull(event);
        assertEquals("Hackathon TN", event.getTitle());
        assertEquals(start, event.getStartDate());
        assertEquals(deadline, event.getDeadline());
        assertEquals(2, event.getMaxVip());
        assertEquals(100, event.getVipPrice());
    }

    @Test
    @DisplayName("Should expose default tier quotas when omitted (builder)")
    void shouldUseBuilderDefaultsForCapacity() {
        Instant start = Instant.parse("2026-06-02T09:00:00Z");
        Instant deadline = Instant.parse("2026-07-02T09:00:00Z");

        Event event = Event.builder()
                .title("Minimal")
                .description("desc")
                .requirements("req")
                .successMetrics("metric")
                .startDate(start)
                .deadline(deadline)
                .build();

        assertEquals(0, event.getMaxVip());
        assertEquals(0, event.getGoldPrice());
        assertNotNull(event.getPartners());
        assertTrue(event.getPartners().isEmpty());
    }

    @Test
    @DisplayName("Should mutate description through setters from Lombok")
    void shouldUpdateTitleThroughSetters() {
        Event event = new Event();

        assertNull(event.getTitle());
        event.setTitle("Kickoff workshop");
        event.setDescription("Deep dive");

        assertEquals("Kickoff workshop", event.getTitle());
        assertEquals("Deep dive", event.getDescription());
    }
}
