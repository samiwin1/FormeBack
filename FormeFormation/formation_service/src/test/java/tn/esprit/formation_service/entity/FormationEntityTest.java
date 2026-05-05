package tn.esprit.formation_service.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Formation entity unit tests")
class FormationEntityTest {

    @Test
    @DisplayName("Should expose title and category after setting fields")
    void shouldCreateFormationWithValidData() {
        Formation formation = new Formation();
        formation.setTitle("Spring Cloud");
        formation.setCategory("DEVOPS");

        assertNotNull(formation);
        assertEquals("Spring Cloud", formation.getTitle());
        assertEquals("DEVOPS", formation.getCategory());
    }

    @Test
    @DisplayName("Should start with null mutable fields until set")
    void shouldHandleNullFieldsInitially() {
        Formation formation = new Formation();

        assertNotNull(formation);
        assertNull(formation.getTitle());
        assertNull(formation.getDescription());
        assertNull(formation.getCreated_by());
    }

    @Test
    @DisplayName("Should update title correctly when reassigned")
    void shouldUpdateTitleCorrectly() {
        Formation formation = new Formation();
        formation.setTitle("First");
        assertEquals("First", formation.getTitle());

        formation.setTitle("Renamed formation");
        assertEquals("Renamed formation", formation.getTitle());
    }
}
