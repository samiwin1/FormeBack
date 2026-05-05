package tn.esprit.events_service.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.events_service.dto.CreateEventRequest;
import tn.esprit.events_service.dto.EventFullResponse;
import tn.esprit.events_service.exception.ForbiddenException;
import tn.esprit.events_service.service.EventService;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventController unit tests")
class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    @Test
    @DisplayName("Admin listing should bubble service response")
    void listAllDelegatesToServiceForAdminRole() {
        EventFullResponse row = stubResponse(9L);
        when(eventService.listAll()).thenReturn(Collections.singletonList(row));

        List<EventFullResponse> result = eventController.list("ADMIN");

        assertEquals(1, result.size());
        assertEquals(9L, result.get(0).getId());
        verify(eventService).listAll();
    }

    @Test
    @DisplayName("Non-admin callers should be rejected for list endpoint")
    void listShouldRejectVisitorsWithoutElevatedRole() {
        assertThrows(ForbiddenException.class, () -> eventController.list(null));
        assertThrows(ForbiddenException.class, () -> eventController.list("USER"));
        verify(eventService, never()).listAll();
    }

    @Test
    @DisplayName("Create should honour admin-only guard")
    void createRequiresAdminRole() {
        CreateEventRequest request = stubRequestPayload();
        EventFullResponse response = stubResponse(100L);

        assertThrows(ForbiddenException.class,
                () -> eventController.create(request, null));

        when(eventService.create(request)).thenReturn(response);
        EventFullResponse result = eventController.create(request, "ADMIN");

        assertEquals(100L, result.getId());
        verify(eventService).create(request);
    }

    @Test
    @DisplayName("Catalog endpoint should omit admin header filtering")
    void catalogDelegatesWithOptionalViewer() {
        when(eventService.listCatalog(55L)).thenReturn(Collections.emptyList());

        List<EventFullResponse> rows = eventController.catalog(55L);

        assertTrue(rows.isEmpty());
        verify(eventService).listCatalog(55L);
    }

    @Test
    @DisplayName("Delete should forward to service once admin authorised")
    void deleteCallsServiceUnderAdminGate() {
        assertThrows(ForbiddenException.class,
                () -> eventController.delete(5L, "PARTNER"));

        eventController.delete(5L, "ADMIN");

        verify(eventService).delete(5L);
    }

    private CreateEventRequest stubRequestPayload() {
        CreateEventRequest payload = new CreateEventRequest();
        payload.setTitle("Conf");
        payload.setDescription("Conference");
        payload.setRequirements("Badge");
        payload.setSuccessMetrics("Happy attendees");
        payload.setStartDate(Instant.parse("2029-01-01T00:00:00Z"));
        payload.setDeadline(Instant.parse("2029-01-10T00:00:00Z"));
        payload.setMaxVip(0);
        payload.setMaxGold(0);
        payload.setMaxSilver(0);
        payload.setVipPrice(0);
        payload.setGoldPrice(0);
        payload.setSilverPrice(0);
        return payload;
    }

    private EventFullResponse stubResponse(long id) {
        Instant start = Instant.parse("2030-01-01T00:00:00Z");
        Instant deadline = Instant.parse("2031-02-02T00:00:00Z");
        return EventFullResponse.builder()
                .id(id)
                .title("Sample-" + id)
                .description("d")
                .requirements("r")
                .successMetrics("m")
                .startDate(start)
                .deadline(deadline)
                .maxVip(0)
                .maxGold(0)
                .maxSilver(0)
                .vipPrice(0)
                .goldPrice(0)
                .silverPrice(0)
                .currentVip(0)
                .currentGold(0)
                .currentSilver(0)
                .participantCount(0)
                .build();
    }
}
