package com.techbyte.servicedesk.service;

import com.techbyte.servicedesk.domain.enums.Role;
import com.techbyte.servicedesk.domain.enums.TicketStatus;
import com.techbyte.servicedesk.dto.DashboardSummaryResponse;
import com.techbyte.servicedesk.repository.TicketRepository;
import com.techbyte.servicedesk.security.CustomUserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final TicketRepository ticketRepository;

    public DashboardService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(CustomUserPrincipal principal) {
        long total;
        long open;
        long inProgress;
        long resolved;
        long closed;

        if (principal.getRole() == Role.STAFF) {
            Long userId = principal.getId();
            total = ticketRepository.countByCreatedById(userId);
            open = ticketRepository.countByCreatedByIdAndStatus(userId, TicketStatus.OPEN);
            inProgress = ticketRepository.countByCreatedByIdAndStatus(userId, TicketStatus.IN_PROGRESS);
            resolved = ticketRepository.countByCreatedByIdAndStatus(userId, TicketStatus.RESOLVED);
            closed = ticketRepository.countByCreatedByIdAndStatus(userId, TicketStatus.CLOSED);
        } else if (principal.getRole() == Role.TECHNICIAN) {
            Long userId = principal.getId();
            total = ticketRepository.countByAssignedToId(userId);
            open = ticketRepository.countByAssignedToIdAndStatus(userId, TicketStatus.OPEN);
            inProgress = ticketRepository.countByAssignedToIdAndStatus(userId, TicketStatus.IN_PROGRESS);
            resolved = ticketRepository.countByAssignedToIdAndStatus(userId, TicketStatus.RESOLVED);
            closed = ticketRepository.countByAssignedToIdAndStatus(userId, TicketStatus.CLOSED);
        } else {
            total = ticketRepository.count();
            open = ticketRepository.countByStatus(TicketStatus.OPEN);
            inProgress = ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
            resolved = ticketRepository.countByStatus(TicketStatus.RESOLVED);
            closed = ticketRepository.countByStatus(TicketStatus.CLOSED);
        }

        return new DashboardSummaryResponse(total, open, inProgress, resolved, closed);
    }
}

