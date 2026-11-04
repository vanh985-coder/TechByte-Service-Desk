package com.techbyte.servicedesk.controller;

import com.techbyte.servicedesk.domain.enums.TicketPriority;
import com.techbyte.servicedesk.domain.enums.TicketStatus;
import com.techbyte.servicedesk.dto.*;
import com.techbyte.servicedesk.security.CustomUserPrincipal;
import com.techbyte.servicedesk.service.TicketService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketSummaryDto> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        TicketSummaryDto created = ticketService.createTicket(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<TicketSummaryDto>> listTickets(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ticketService.listTickets(search, status, priority, page, limit, principal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDetailDto> getTicketDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ticketService.getTicketDetail(id, principal));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TicketSummaryDto> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request, principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTicket(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        ticketService.deleteTicket(id, principal);
        return ResponseEntity.ok(Map.of("message", "Xóa ticket thành công"));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<TicketSummaryDto> assignTicket(
            @PathVariable Long id,
            @Valid @RequestBody AssignTicketRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ticketService.assignTicket(id, request, principal));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketSummaryDto> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ticketService.updateTicketStatus(id, request, principal));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<TicketCommentDto> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        TicketCommentDto created = ticketService.addComment(id, request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
