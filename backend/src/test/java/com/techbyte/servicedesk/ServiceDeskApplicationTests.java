package com.techbyte.servicedesk;

import com.techbyte.servicedesk.domain.enums.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ServiceDeskApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void stateMachineFollowsStrictForwardRules() {
        assertTrue(TicketStatus.OPEN.canTransitionTo(TicketStatus.IN_PROGRESS));
        assertTrue(TicketStatus.IN_PROGRESS.canTransitionTo(TicketStatus.RESOLVED));
        assertTrue(TicketStatus.RESOLVED.canTransitionTo(TicketStatus.CLOSED));

        // Direct cancellation (OPEN -> CLOSED) and skipping/reverting are forbidden
        assertFalse(TicketStatus.OPEN.canTransitionTo(TicketStatus.CLOSED));
        assertFalse(TicketStatus.OPEN.canTransitionTo(TicketStatus.RESOLVED));
        assertFalse(TicketStatus.RESOLVED.canTransitionTo(TicketStatus.IN_PROGRESS));
        assertFalse(TicketStatus.CLOSED.canTransitionTo(TicketStatus.OPEN));
    }
}

