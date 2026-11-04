package com.techbyte.servicedesk;

import com.techbyte.servicedesk.controller.HealthController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class HealthControllerTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final ApplicationAvailability availability = mock(ApplicationAvailability.class);
    private final HealthController controller = new HealthController(jdbc, availability);

    @Test
    void refusesTrafficBeforeStartupAndSeedFinish() {
        when(availability.getReadinessState()).thenReturn(ReadinessState.REFUSING_TRAFFIC);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, controller.ready().getStatusCode());
        verifyNoInteractions(jdbc);
    }

    @Test
    void acceptsTrafficOnlyWhenStartupAndDatabaseAreReady() {
        when(availability.getReadinessState()).thenReturn(ReadinessState.ACCEPTING_TRAFFIC);
        when(jdbc.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        var response = controller.ready();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("READY", response.getBody().get("status"));
        assertEquals("UP", response.getBody().get("database"));
    }

    @Test
    void returnsUnavailableWhenTheDatabaseConnectionFails() {
        when(availability.getReadinessState()).thenReturn(ReadinessState.ACCEPTING_TRAFFIC);
        when(jdbc.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new DataAccessResourceFailureException("test database unavailable"));
        var response = controller.ready();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("DOWN", response.getBody().get("database"));
    }
}
