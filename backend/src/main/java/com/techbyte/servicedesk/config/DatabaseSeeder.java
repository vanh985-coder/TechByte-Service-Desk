package com.techbyte.servicedesk.config;

import com.techbyte.servicedesk.domain.entity.Ticket;
import com.techbyte.servicedesk.domain.entity.TicketComment;
import com.techbyte.servicedesk.domain.entity.TicketHistory;
import com.techbyte.servicedesk.domain.entity.User;
import com.techbyte.servicedesk.domain.enums.Role;
import com.techbyte.servicedesk.domain.enums.TicketPriority;
import com.techbyte.servicedesk.domain.enums.TicketStatus;
import com.techbyte.servicedesk.repository.TicketCommentRepository;
import com.techbyte.servicedesk.repository.TicketHistoryRepository;
import com.techbyte.servicedesk.repository.TicketRepository;
import com.techbyte.servicedesk.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final TicketHistoryRepository historyRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean seedEnabled;

    public DatabaseSeeder(
            UserRepository userRepository,
            TicketRepository ticketRepository,
            TicketCommentRepository commentRepository,
            TicketHistoryRepository historyRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.enabled:true}") boolean seedEnabled) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.historyRepository = historyRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedEnabled = seedEnabled;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled || userRepository.count() > 0) {
            return;
        }

        log.info("Initializing demo dataset (08 users, 20 tickets, 10 comments)...");
        String defaultHash = passwordEncoder.encode("Password@123");

        User admin1 = userRepository.save(new User("admin@techbyte.vn", defaultHash, "Trần Quản Trị (Admin)", Role.ADMIN));
        User admin2 = userRepository.save(new User("manager@techbyte.vn", defaultHash, "Lê Quản Lý (Admin)", Role.ADMIN));
        User tech1 = userRepository.save(new User("tech1@techbyte.vn", defaultHash, "Nguyễn Kỹ Thuật 1", Role.TECHNICIAN));
        User tech2 = userRepository.save(new User("tech2@techbyte.vn", defaultHash, "Phạm Kỹ Thuật 2", Role.TECHNICIAN));
        User tech3 = userRepository.save(new User("tech3@techbyte.vn", defaultHash, "Hoàng Kỹ Thuật 3", Role.TECHNICIAN));
        User staff1 = userRepository.save(new User("staff1@techbyte.vn", defaultHash, "Vũ Nhân Viên 1", Role.STAFF));
        User staff2 = userRepository.save(new User("staff2@techbyte.vn", defaultHash, "Đặng Nhân Viên 2", Role.STAFF));
        User staff3 = userRepository.save(new User("staff3@techbyte.vn", defaultHash, "Bùi Nhân Viên 3", Role.STAFF));

        List<User> staffs = List.of(staff1, staff2, staff3);
        List<User> techs = List.of(tech1, tech2, tech3);
        TicketPriority[] priorities = TicketPriority.values();
        TicketStatus[] statuses = TicketStatus.values();

        List<Ticket> savedTickets = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Ticket ticket = new Ticket();
            ticket.setCode(String.format("TCK-%04d", i));
            ticket.setTitle("Yêu cầu hỗ trợ kỹ thuật mẫu số #" + i);
            ticket.setDescription("Mô tả chi tiết sự cố hệ thống giả lập phục vụ kiểm thử #" + i);
            ticket.setPriority(priorities[i % priorities.length]);
            TicketStatus status = statuses[i % statuses.length];
            ticket.setStatus(status);
            ticket.setCreatedBy(staffs.get(i % staffs.size()));

            User assignedTech = techs.get(i % techs.size());
            if (status != TicketStatus.OPEN || i % 2 == 0) {
                ticket.setAssignedTo(assignedTech);
            }

            Ticket saved = ticketRepository.save(ticket);
            savedTickets.add(saved);

            if (status != TicketStatus.OPEN) {
                historyRepository.save(new TicketHistory(saved, assignedTech, TicketStatus.OPEN, TicketStatus.IN_PROGRESS));
            }
            if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
                historyRepository.save(new TicketHistory(saved, assignedTech, TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED));
            }
            if (status == TicketStatus.CLOSED) {
                historyRepository.save(new TicketHistory(saved, admin1, TicketStatus.RESOLVED, TicketStatus.CLOSED));
            }
        }

        for (int c = 0; c < 10; c++) {
            Ticket targetTicket = savedTickets.get(c);
            User commentAuthor = (c % 2 == 0) ? targetTicket.getCreatedBy() : tech1;
            commentRepository.save(new TicketComment(
                    targetTicket,
                    commentAuthor,
                    "Bình luận trao đổi xử lý sự cố mẫu số #" + (c + 1)
            ));
        }

        log.info("Demo dataset initialized successfully.");
    }
}

