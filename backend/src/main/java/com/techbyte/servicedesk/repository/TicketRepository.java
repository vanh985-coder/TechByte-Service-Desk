package com.techbyte.servicedesk.repository;

import com.techbyte.servicedesk.domain.entity.Ticket;
import com.techbyte.servicedesk.domain.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    long countByStatus(TicketStatus status);
    long countByCreatedById(Long createdById);
    long countByCreatedByIdAndStatus(Long createdById, TicketStatus status);
    long countByAssignedToId(Long assignedToId);
    long countByAssignedToIdAndStatus(Long assignedToId, TicketStatus status);
}

