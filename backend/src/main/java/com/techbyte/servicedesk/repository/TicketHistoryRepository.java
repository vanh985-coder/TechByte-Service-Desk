package com.techbyte.servicedesk.repository;

import com.techbyte.servicedesk.domain.entity.TicketHistory;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {
    @EntityGraph(attributePaths = {"actor"})
    List<TicketHistory> findByTicketIdOrderByChangedAtAsc(Long ticketId);

    void deleteByTicketId(Long ticketId);
}

