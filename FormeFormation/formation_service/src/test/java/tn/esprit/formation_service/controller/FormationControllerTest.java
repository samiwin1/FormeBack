package tn.esprit.formation_service.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tn.esprit.formation_service.entity.Formation;
import tn.esprit.formation_service.service.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FormationController unit tests")
class FormationControllerTest {

    @Mock
    private FormationService formationService;
    @Mock
    private FormationProgressService formationProgressService;
    @Mock
    private FormationSearchService formationSearchService;
    @Mock
    private ResultEvaluationService resultEvaluationService;
    @Mock
    private ResultExamenService resultExamenService;
    @Mock
    private ContentLockService contentLockService;

    @InjectMocks
    private FormationController formationController;

    @Test
    @DisplayName("Should persist formation via service")
    void shouldInvokeServiceOnCreate() {
        Formation request = formation("Inbound");
        Formation saved = formation("Inbound");
        saved.setId(5L);
        when(formationService.save(request)).thenReturn(saved);

        ResponseEntity<Formation> response = formationController.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5L, response.getBody().getId());
        verify(formationService).save(request);
    }

    @Test
    @DisplayName("Should return formation when id exists")
    void shouldReturnBodyWhenFormationExists() {
        Formation existing = formation("Catalog");
        existing.setId(10L);
        when(formationService.findById(10L)).thenReturn(Optional.of(existing));

        ResponseEntity<Formation> response = formationController.getById(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody().getId());
        verify(formationService).findById(10L);
    }

    @Test
    @DisplayName("Should propagate 404 when formation missing")
    void shouldRespondNotFoundForUnknownId() {
        when(formationService.findById(404L)).thenReturn(Optional.empty());

        ResponseEntity<Formation> response = formationController.getById(404L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(formationService).findById(404L);
    }

    @Test
    @DisplayName("Should return full list via service when pagination params absent")
    void shouldBypassSearchWhenListingWithoutFilters() {
        Formation one = formation("One");
        when(formationService.findAll()).thenReturn(Collections.singletonList(one));

        ResponseEntity<?> response = formationController.getAll(null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(List.class, response.getBody());
        assertEquals(1, ((List<?>) response.getBody()).size());
        verify(formationSearchService, never()).findAllFiltered(anyString(), any(), any(), any(), any());
        verify(formationService).findAll();
    }

    @Test
    @DisplayName("Should delete formation when repository layer reports presence")
    void shouldIssueDeleteThroughServiceWhenExists() {
        Formation existing = formation("DeleteMe");
        existing.setId(3L);
        when(formationService.findById(3L)).thenReturn(Optional.of(existing));

        ResponseEntity<Void> response = formationController.delete(3L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(formationService).deleteById(3L);
    }

    private Formation formation(String slug) {
        Formation formation = new Formation();
        formation.setTitle(slug);
        formation.setDescription("desc");
        formation.setStatus("published");
        return formation;
    }
}
