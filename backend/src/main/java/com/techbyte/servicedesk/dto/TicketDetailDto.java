package com.techbyte.servicedesk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techbyte.servicedesk.domain.entity.Ticket;
import com.techbyte.servicedesk.domain.enums.TicketPriority;
import com.techbyte.servicedesk.domain.enums.TicketStatus;
import java.time.Instant;
import java.util.List;

public class TicketDetailDto {

    private Long id;
    private String code;
    private String title;
    private String description;
    private TicketPriority priority;
    private TicketStatus status;

    @JsonProperty("created_by")
    private UserSummaryDto createdBy;

    @JsonProperty("assigned_to")
    private UserSummaryDto assignedTo;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    private List<TicketCommentDto> comments;
    private List<TicketHistoryDto> history;

    public TicketDetailDto() {
    }

    public static TicketDetailDto fromEntity(
            Ticket ticket,
            List<TicketCommentDto> comments,
            List<TicketHistoryDto> history) {
        TicketDetailDto dto = new TicketDetailDto();
        dto.setId(ticket.getId());
        dto.setCode(ticket.getCode());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setPriority(ticket.getPriority());
        dto.setStatus(ticket.getStatus());
        dto.setCreatedBy(UserSummaryDto.fromEntity(ticket.getCreatedBy()));
        dto.setAssignedTo(UserSummaryDto.fromEntity(ticket.getAssignedTo()));
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setComments(comments);
        dto.setHistory(history);
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public UserSummaryDto getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserSummaryDto createdBy) {
        this.createdBy = createdBy;
    }

    public UserSummaryDto getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UserSummaryDto assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TicketCommentDto> getComments() {
        return comments;
    }

    public void setComments(List<TicketCommentDto> comments) {
        this.comments = comments;
    }

    public List<TicketHistoryDto> getHistory() {
        return history;
    }

    public void setHistory(List<TicketHistoryDto> history) {
        this.history = history;
    }
}

