package com.techbyte.servicedesk.service;

import com.techbyte.servicedesk.domain.entity.Ticket;
import com.techbyte.servicedesk.domain.entity.TicketComment;
import com.techbyte.servicedesk.domain.entity.TicketHistory;
import com.techbyte.servicedesk.domain.entity.User;
import com.techbyte.servicedesk.domain.enums.Role;
import com.techbyte.servicedesk.domain.enums.TicketPriority;
import com.techbyte.servicedesk.domain.enums.TicketStatus;
import com.techbyte.servicedesk.dto.*;
import com.techbyte.servicedesk.exception.BadRequestException;
import com.techbyte.servicedesk.exception.ResourceNotFoundException;
import com.techbyte.servicedesk.repository.TicketCommentRepository;
import com.techbyte.servicedesk.repository.TicketHistoryRepository;
import com.techbyte.servicedesk.repository.TicketRepository;
import com.techbyte.servicedesk.repository.UserRepository;
import com.techbyte.servicedesk.security.CustomUserPrincipal;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketCommentRepository commentRepository;
    private final TicketHistoryRepository historyRepository;

    public TicketService(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            TicketCommentRepository commentRepository,
            TicketHistoryRepository historyRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public TicketSummaryDto createTicket(CreateTicketRequest request, CustomUserPrincipal principal) {
        if (principal.getRole() == Role.TECHNICIAN) {
            throw new AccessDeniedException("Kỹ thuật viên không có quyền tạo ticket mới");
        }

        User creator = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng hiện tại"));

        Ticket ticket = new Ticket();
        ticket.setCode("TCK-TEMP-" + UUID.randomUUID().toString().substring(0, 8));
        ticket.setTitle(request.getTitle().trim());
        ticket.setDescription(request.getDescription().trim());
        ticket.setPriority(request.getPriority() != null ? request.getPriority() : TicketPriority.MEDIUM);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCreatedBy(creator);

        Ticket saved = ticketRepository.save(ticket);
        saved.setCode(String.format("TCK-%04d", saved.getId()));
        saved = ticketRepository.save(saved);

        return TicketSummaryDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TicketSummaryDto> listTickets(
            String search,
            TicketStatus status,
            TicketPriority priority,
            int page,
            int limit,
            CustomUserPrincipal principal) {
        int safePage = Math.max(page, 1);
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        Pageable pageable = PageRequest.of(safePage - 1, safeLimit, Sort.by(Sort.Direction.DESC, "updatedAt"));

        Specification<Ticket> spec = (root, query, cb) -> {
            if (query != null && Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("createdBy", JoinType.LEFT);
                root.fetch("assignedTo", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            // Role-based access scope
            if (principal.getRole() == Role.STAFF) {
                predicates.add(cb.equal(root.get("createdBy").get("id"), principal.getId()));
            } else if (principal.getRole() == Role.TECHNICIAN) {
                predicates.add(cb.equal(root.get("assignedTo").get("id"), principal.getId()));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate codeMatch = cb.like(cb.lower(root.get("code")), pattern);
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                predicates.add(cb.or(codeMatch, titleMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Ticket> resultPage = ticketRepository.findAll(spec, pageable);
        List<TicketSummaryDto> items = resultPage.getContent().stream()
                .map(TicketSummaryDto::fromEntity)
                .toList();

        return new PagedResponse<>(
                items,
                safePage,
                safeLimit,
                resultPage.getTotalElements(),
                resultPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public TicketDetailDto getTicketDetail(Long ticketId, CustomUserPrincipal principal) {
        Ticket ticket = findTicketOrThrow(ticketId);
        verifyTicketReadAccess(ticket, principal);

        List<TicketCommentDto> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(TicketCommentDto::fromEntity)
                .toList();

        List<TicketHistoryDto> history = historyRepository.findByTicketIdOrderByChangedAtAsc(ticketId).stream()
                .map(TicketHistoryDto::fromEntity)
                .toList();

        return TicketDetailDto.fromEntity(ticket, comments, history);
    }

    @Transactional
    public TicketSummaryDto updateTicket(Long ticketId, UpdateTicketRequest request, CustomUserPrincipal principal) {
        Ticket ticket = findTicketOrThrow(ticketId);

        if (principal.getRole() == Role.TECHNICIAN) {
            throw new AccessDeniedException("TECHNICIAN không có quyền sửa thông tin ticket");
        }

        if (principal.getRole() == Role.STAFF) {
            if (!ticket.getCreatedBy().getId().equals(principal.getId())) {
                throw new AccessDeniedException("STAFF chỉ được sửa ticket của chính mình");
            }
            if (ticket.getStatus() != TicketStatus.OPEN) {
                throw new BadRequestException("STAFF chỉ có thể sửa ticket khi còn ở trạng thái OPEN");
            }
        } else if (principal.getRole() == Role.ADMIN) {
            if (ticket.getStatus() == TicketStatus.CLOSED) {
                throw new BadRequestException("Không thể sửa ticket đã ở trạng thái CLOSED");
            }
        }

        if (request.getTitle() != null) {
            if (!StringUtils.hasText(request.getTitle())) {
                throw new BadRequestException("Tiêu đề không được để trống");
            }
            ticket.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            if (!StringUtils.hasText(request.getDescription())) {
                throw new BadRequestException("Mô tả không được để trống");
            }
            ticket.setDescription(request.getDescription().trim());
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }

        ticket.setUpdatedAt(Instant.now());
        Ticket saved = ticketRepository.save(ticket);
        return TicketSummaryDto.fromEntity(saved);
    }

    @Transactional
    public void deleteTicket(Long ticketId, CustomUserPrincipal principal) {
        if (principal.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Chỉ ADMIN mới có quyền xóa ticket");
        }

        Ticket ticket = findTicketOrThrow(ticketId);
        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw new BadRequestException("Chỉ được phép xóa ticket khi đang ở trạng thái OPEN để bảo toàn lịch sử truy vết");
        }

        commentRepository.deleteByTicketId(ticketId);
        historyRepository.deleteByTicketId(ticketId);
        ticketRepository.delete(ticket);
    }

    @Transactional
    public TicketSummaryDto assignTicket(Long ticketId, AssignTicketRequest request, CustomUserPrincipal principal) {
        if (principal.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Chỉ ADMIN mới có quyền phân công người xử lý ticket");
        }

        Ticket ticket = findTicketOrThrow(ticketId);
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("Không thể phân công lại ticket đã CLOSED");
        }

        User assignee = userRepository.findById(request.getAssignedTo())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy người dùng được phân công"));

        if (assignee.getRole() != Role.TECHNICIAN) {
            throw new BadRequestException("Người được phân công xử lý bắt buộc phải có vai trò TECHNICIAN");
        }

        ticket.setAssignedTo(assignee);
        ticket.setUpdatedAt(Instant.now());
        Ticket saved = ticketRepository.save(ticket);
        return TicketSummaryDto.fromEntity(saved);
    }

    @Transactional
    public TicketSummaryDto updateTicketStatus(
            Long ticketId,
            UpdateStatusRequest request,
            CustomUserPrincipal principal) {
        if (principal.getRole() == Role.STAFF) {
            throw new AccessDeniedException("STAFF không có quyền chuyển trạng thái xử lý của ticket");
        }

        Ticket ticket = findTicketOrThrow(ticketId);

        if (principal.getRole() == Role.TECHNICIAN) {
            if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(principal.getId())) {
                throw new AccessDeniedException("TECHNICIAN chỉ được phép đổi trạng thái của ticket được giao cho mình");
            }
        }

        TicketStatus oldStatus = ticket.getStatus();
        TicketStatus newStatus = request.getStatus();

        if (!oldStatus.canTransitionTo(newStatus)) {
            throw new BadRequestException(String.format(
                    "Chuyển trạng thái không hợp lệ từ %s sang %s (chỉ cho phép OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED)",
                    oldStatus, newStatus
            ));
        }

        User actor = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người thực hiện"));

        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(Instant.now());
        Ticket savedTicket = ticketRepository.save(ticket);

        TicketHistory history = new TicketHistory(savedTicket, actor, oldStatus, newStatus);
        historyRepository.save(history);

        return TicketSummaryDto.fromEntity(savedTicket);
    }

    @Transactional
    public TicketCommentDto addComment(
            Long ticketId,
            CreateCommentRequest request,
            CustomUserPrincipal principal) {
        Ticket ticket = findTicketOrThrow(ticketId);
        verifyTicketReadAccess(ticket, principal);

        User author = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người bình luận"));

        TicketComment comment = new TicketComment(ticket, author, request.getContent().trim());
        TicketComment saved = commentRepository.save(comment);

        ticket.setUpdatedAt(Instant.now());
        ticketRepository.save(ticket);

        return TicketCommentDto.fromEntity(saved);
    }

    private Ticket findTicketOrThrow(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ticket với ID: " + ticketId));
    }

    public void verifyTicketReadAccess(Ticket ticket, CustomUserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN) {
            return;
        }
        if (principal.getRole() == Role.STAFF) {
            if (!ticket.getCreatedBy().getId().equals(principal.getId())) {
                throw new AccessDeniedException("STAFF chỉ được phép truy cập ticket do chính mình tạo");
            }
            return;
        }
        if (principal.getRole() == Role.TECHNICIAN) {
            if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(principal.getId())) {
                throw new AccessDeniedException("TECHNICIAN chỉ được phép truy cập ticket được giao cho mình");
            }
        }
    }
}
