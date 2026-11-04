package com.techbyte.servicedesk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class AssignTicketRequest {

    @NotNull(message = "assigned_to không được để trống")
    @JsonProperty("assigned_to")
    private Long assignedTo;

    public AssignTicketRequest() {
    }

    public AssignTicketRequest(Long assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Long getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Long assignedTo) {
        this.assignedTo = assignedTo;
    }
}

