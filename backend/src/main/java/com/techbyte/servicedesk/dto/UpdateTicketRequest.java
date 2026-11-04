package com.techbyte.servicedesk.dto;

import com.techbyte.servicedesk.domain.enums.TicketPriority;
import jakarta.validation.constraints.Size;

public class UpdateTicketRequest {

    @Size(min = 1, max = 200, message = "Tiêu đề phải từ 1 đến 200 ký tự")
    private String title;

    @Size(min = 1, max = 2000, message = "Mô tả phải từ 1 đến 2000 ký tự")
    private String description;

    private TicketPriority priority;

    public UpdateTicketRequest() {
    }

    public UpdateTicketRequest(String title, String description, TicketPriority priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }
}

