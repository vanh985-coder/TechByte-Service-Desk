package com.techbyte.servicedesk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techbyte.servicedesk.domain.entity.TicketComment;
import java.time.Instant;

public class TicketCommentDto {

    private Long id;

    @JsonProperty("ticket_id")
    private Long ticketId;

    private UserSummaryDto author;
    private String content;

    @JsonProperty("created_at")
    private Instant createdAt;

    public TicketCommentDto() {
    }

    public static TicketCommentDto fromEntity(TicketComment comment) {
        TicketCommentDto dto = new TicketCommentDto();
        dto.setId(comment.getId());
        dto.setTicketId(comment.getTicket().getId());
        dto.setAuthor(UserSummaryDto.fromEntity(comment.getAuthor()));
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
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

    public UserSummaryDto getAuthor() {
        return author;
    }

    public void setAuthor(UserSummaryDto author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

