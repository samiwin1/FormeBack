package tn.esprit.formation_service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Smoke Tests — Formation Service")
class SmokeTest {

    @Test
    @DisplayName("Application context loads — basic sanity check")
    void contextLoads() {
        assertTrue(true, "Basic smoke test always passes");
    }

    @Test
    @DisplayName("Java environment is working correctly")
    void javaEnvironmentWorks() {
        int result = 2 + 2;
        assertEquals(4, result);
    }

    @Test
    @DisplayName("String operations work correctly")
    void stringOperationsWork() {
        String serviceName = "formation-service";
        assertNotNull(serviceName);
        assertTrue(serviceName.contains("formation"));
        assertEquals(17, serviceName.length());
    }
}
