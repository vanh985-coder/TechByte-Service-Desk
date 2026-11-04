package com.techbyte.servicedesk.domain.enums;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    /**
     * Strict forward-only state machine:
     * OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED
     * Direct cancellation (OPEN -> CLOSED) or skipping/reverting is prohibited.
     */
    public boolean canTransitionTo(TicketStatus nextStatus) {
        if (nextStatus == null) {
            return false;
        }
        return switch (this) {
            case OPEN -> nextStatus == IN_PROGRESS;
            case IN_PROGRESS -> nextStatus == RESOLVED;
            case RESOLVED -> nextStatus == CLOSED;
            case CLOSED -> false;
        };
    }
}

