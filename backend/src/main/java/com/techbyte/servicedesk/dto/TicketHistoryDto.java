package com.techbyte.servicedesk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techbyte.servicedesk.domain.entity.TicketHistory;
import com.techbyte.servicedesk.domain.enums.TicketStatus;
import java.time.Instant;

public class TicketHistoryDto {

    private Long id;

    @JsonProperty("ticket_id")
    private Long ticketId;

    private UserSummaryDto actor;

    @JsonProperty("old_status")
    private TicketStatus oldStatus;

    @JsonProperty("new_status")
    private TicketStatus newStatus;

    @JsonProperty("changed_at")
    private Instant changedAt;

    public TicketHistoryDto() {
    }

    public static TicketHistoryDto fromEntity(TicketHistory history) {
        TicketHistoryDto dto = new TicketHistoryDto();
        dto.setId(history.getId());
        dto.setTicketId(history.getTicket().getId());
        dto.setActor(UserSummaryDto.fromEntity(history.getActor()));
        dto.setOldStatus(history.getOldStatus());
        dto.setNewStatus(history.getNewStatus());
        dto.setChangedAt(history.getChangedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public UserSummaryDto getActor() {
        return actor;
    }

    public void setActor(UserSummaryDto actor) {
        this.actor = actor;
    }

    public TicketStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(TicketStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public TicketStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(TicketStatus newStatus) {
        this.newStatus = newStatus;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }
}

