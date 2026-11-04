package com.techbyte.servicedesk.dto;

import com.techbyte.servicedesk.domain.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull(message = "Trạng thái mới không được để trống")
    private TicketStatus status;

    public UpdateStatusRequest() {
    }

    public UpdateStatusRequest(TicketStatus status) {
        this.status = status;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }
}

