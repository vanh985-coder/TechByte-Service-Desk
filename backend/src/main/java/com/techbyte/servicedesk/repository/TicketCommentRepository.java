package com.techbyte.servicedesk.repository;

import com.techbyte.servicedesk.domain.entity.TicketComment;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {
    @EntityGraph(attributePaths = {"author"})
    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    void deleteByTicketId(Long ticketId);
}

