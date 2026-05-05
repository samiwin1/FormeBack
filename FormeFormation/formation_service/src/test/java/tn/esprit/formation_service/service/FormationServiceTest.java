package tn.esprit.formation_service.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.formation_service.entity.Formation;
import tn.esprit.formation_service.repository.FormationRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FormationServiceImpl unit tests")
class FormationServiceTest {

    @Mock
    private FormationRepository formationRepository;

    @InjectMocks
    private FormationServiceImpl formationService;

    @Test
    @DisplayName("Should return all formations")
    void shouldReturnAllFormations() {
        Formation first = sampleFormation("A");
        Formation second = sampleFormation("B");
        when(formationRepository.findAll()).thenReturn(List.of(first, second));

        List<Formation> result = formationService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(formationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should delegate save to repository")
    void shouldPersistFormationViaRepository() {
        Formation incoming = sampleFormation("New");
        when(formationRepository.save(any(Formation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Formation saved = formationService.save(incoming);

        assertSame(incoming, saved);
        verify(formationRepository).save(incoming);
    }

    @Test
    @DisplayName("Should return formation when repository contains id")
    void shouldFindByIdWhenPresent() {
        Formation formation = sampleFormation("Found");
        formation.setId(42L);
        when(formationRepository.findById(42L)).thenReturn(Optional.of(formation));

        Optional<Formation> result = formationService.findById(42L);

        assertTrue(result.isPresent());
        assertEquals(42L, result.get().getId());
        verify(formationRepository).findById(42L);
    }

    @Test
    @DisplayName("Should delete formation by id via repository")
    void shouldDelegateDeleteById() {
        doNothing().when(formationRepository).deleteById(7L);

        formationService.deleteById(7L);

        verify(formationRepository).deleteById(7L);
    }

    @Test
    @DisplayName("Should throw when updating unknown formation")
    void shouldThrowWhenUpdateTargetMissing() {
        when(formationRepository.findById(99L)).thenReturn(Optional.empty());

        Formation patch = sampleFormation("Any");

        assertThrows(RuntimeException.class, () -> formationService.update(99L, patch));
        verify(formationRepository).findById(99L);
        verify(formationRepository, never()).save(any());
    }

    private static Formation sampleFormation(String suffix) {
        Formation formation = new Formation();
        formation.setTitle("Title-" + suffix);
        formation.setDescription("Description-" + suffix);
        formation.setCategory("CAT");
        formation.setLevel("L1");
        formation.setStatus("draft");
        return formation;
    }
}
