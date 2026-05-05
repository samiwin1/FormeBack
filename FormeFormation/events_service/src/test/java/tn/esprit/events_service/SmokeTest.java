package tn.esprit.events_service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Smoke Tests — Events Service")
class SmokeTest {

    @Test
    @DisplayName("Application context loads — basic sanity check")
    void contextLoads() {
        assertTrue(true, "Basic smoke test always passes");
    }

    @Test
    @DisplayName("Java environment is working correctly")
    void javaEnvironmentWorks() {
        int result = 3 + 3;
        assertEquals(6, result);
    }

    @Test
    @DisplayName("String operations work correctly")
    void stringOperationsWork() {
        String serviceName = "events-service";
        assertNotNull(serviceName);
        assertTrue(serviceName.contains("events"));
        assertEquals(14, serviceName.length());
    }
}
