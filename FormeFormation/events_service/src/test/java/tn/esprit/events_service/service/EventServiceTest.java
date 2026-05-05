package tn.esprit.events_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.events_service.dto.CreateEventRequest;
import tn.esprit.events_service.dto.EventFullResponse;
import tn.esprit.events_service.entity.Event;
import tn.esprit.events_service.entity.EventTier;
import tn.esprit.events_service.exception.ResourceNotFoundException;
import tn.esprit.events_service.notification.EventEmailService;
import tn.esprit.events_service.repository.EventDepositRepository;
import tn.esprit.events_service.repository.EventParticipantRepository;
import tn.esprit.events_service.repository.EventPartnerRepository;
import tn.esprit.events_service.repository.EventRepository;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventServiceImpl unit tests")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventPartnerRepository eventPartnerRepository;
    @Mock
    private EventParticipantRepository eventParticipantRepository;
    @Mock
    private EventDepositRepository eventDepositRepository;
    @Mock
    private EventEmailService eventEmailService;

    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(
                eventRepository,
                eventPartnerRepository,
                eventParticipantRepository,
                eventDepositRepository,
                eventEmailService
        );
    }

    @Test
    @DisplayName("Should throw when deleting non-existent record")
    void deleteShouldRejectUnknownId() {
        when(eventRepository.existsById(404L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> eventService.delete(404L));
        verify(eventRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should propagate delete to repository")
    void deleteShouldCallRepositoryWhenExists() {
        when(eventRepository.existsById(30L)).thenReturn(true);

        eventService.delete(30L);

        verify(eventRepository).deleteById(30L);
    }

    @Test
    @DisplayName("Catalog should mirror empty repositories")
    void catalogShouldRemainEmptyWithoutEvents() {
        when(eventRepository.findAll()).thenReturn(List.of());

        assertTrue(eventService.listCatalog(null).isEmpty());
        verify(eventParticipantRepository, never()).findByEventIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Create should persist and map response id")
    void createPersistsViaRepositoryAndReturnsDto() {
        CreateEventRequest req = seedRequest();

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event entity = invocation.getArgument(0);
            entity.setId(77L);
            return entity;
        });
        when(eventPartnerRepository.countByEventIdAndTier(eq(77L), eq(EventTier.VIP))).thenReturn(0L);
        when(eventPartnerRepository.countByEventIdAndTier(eq(77L), eq(EventTier.GOLD))).thenReturn(0L);
        when(eventPartnerRepository.countByEventIdAndTier(eq(77L), eq(EventTier.SILVER))).thenReturn(0L);
        when(eventParticipantRepository.countByEventId(eq(77L))).thenReturn(0L);

        EventFullResponse response = eventService.create(req);

        assertEquals(77L, response.getId());
        assertEquals(req.getTitle(), response.getTitle());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("List all should flatten repository stream")
    void listAllShouldCallFindAllOnce() {
        Event entity = seededEvent();

        when(eventRepository.findAll()).thenReturn(List.of(entity));
        when(eventPartnerRepository.countByEventIdAndTier(eq(1L), any(EventTier.class))).thenReturn(0L);
        when(eventParticipantRepository.countByEventId(1L)).thenReturn(0L);

        List<EventFullResponse> rows = eventService.listAll();

        assertEquals(1, rows.size());
        assertEquals("Bootcamp API", rows.get(0).getTitle());
        verify(eventRepository).findAll();
    }

    private CreateEventRequest seedRequest() {
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle("Design sprint");
        request.setDescription("Team collaboration");
        request.setRequirements("Whiteboard access");
        request.setSuccessMetrics("Prototype approved");
        request.setStartDate(Instant.parse("2027-01-15T07:30:00Z"));
        request.setDeadline(Instant.parse("2027-02-01T18:45:00Z"));
        request.setMaxVip(1);
        request.setMaxGold(2);
        request.setMaxSilver(3);
        request.setVipPrice(900);
        request.setGoldPrice(400);
        request.setSilverPrice(150);
        return request;
    }

    private Event seededEvent() {
        return Event.builder()
                .id(1L)
                .title("Bootcamp API")
                .description("desc")
                .requirements("requirements")
                .successMetrics("metrics")
                .startDate(Instant.parse("2028-03-05T07:30:00Z"))
                .deadline(Instant.parse("2028-04-05T07:30:00Z"))
                .maxVip(1)
                .maxGold(1)
                .maxSilver(1)
                .vipPrice(10)
                .goldPrice(5)
                .silverPrice(1)
                .build();
    }
}
