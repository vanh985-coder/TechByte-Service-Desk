package com.techbyte.servicedesk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techbyte.servicedesk.domain.entity.Ticket;
import com.techbyte.servicedesk.domain.entity.User;
import com.techbyte.servicedesk.domain.enums.Role;
import com.techbyte.servicedesk.domain.enums.TicketPriority;
import com.techbyte.servicedesk.domain.enums.TicketStatus;
import com.techbyte.servicedesk.dto.AssignTicketRequest;
import com.techbyte.servicedesk.dto.CreateCommentRequest;
import com.techbyte.servicedesk.dto.UpdateStatusRequest;
import com.techbyte.servicedesk.dto.UpdateTicketRequest;
import com.techbyte.servicedesk.repository.TicketCommentRepository;
import com.techbyte.servicedesk.repository.TicketHistoryRepository;
import com.techbyte.servicedesk.repository.TicketRepository;
import com.techbyte.servicedesk.repository.UserRepository;
import com.techbyte.servicedesk.security.CustomUserPrincipal;
import com.techbyte.servicedesk.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Sprint2WorkflowAndDashboardTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketCommentRepository commentRepository;

    @Autowired
    private TicketHistoryRepository historyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User adminUser;
    private User techUser;
    private User staffUser;

    private String adminToken;
    private String techToken;
    private String staffToken;

    private Ticket openTicket;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        historyRepository.deleteAll();
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = userRepository.save(new User(
                "admin@techbyte.vn",
                passwordEncoder.encode("Admin@123"),
                "Admin TechByte",
                Role.ADMIN
        ));
        techUser = userRepository.save(new User(
                "tech1@techbyte.vn",
                passwordEncoder.encode("Tech@123"),
                "Kỹ Thuật Viên 1",
                Role.TECHNICIAN
        ));
        staffUser = userRepository.save(new User(
                "staff1@techbyte.vn",
                passwordEncoder.encode("Staff@123"),
                "Nhân Viên 1",
                Role.STAFF
        ));

        adminToken = jwtTokenProvider.generateToken(CustomUserPrincipal.fromUser(adminUser));
        techToken = jwtTokenProvider.generateToken(CustomUserPrincipal.fromUser(techUser));
        staffToken = jwtTokenProvider.generateToken(CustomUserPrincipal.fromUser(staffUser));

        openTicket = new Ticket();
        openTicket.setCode("TCK-0001");
        openTicket.setTitle("Sự cố kết nối VPN");
        openTicket.setDescription("Không kết nối được VPN vào mạng công ty");
        openTicket.setPriority(TicketPriority.HIGH);
        openTicket.setStatus(TicketStatus.OPEN);
        openTicket.setCreatedBy(staffUser);
        openTicket = ticketRepository.save(openTicket);
    }

    @Test
    @DisplayName("US-2.1: Phân công ticket chỉ cho phép ADMIN và bắt buộc người nhận có role TECHNICIAN")
    void assignTicketValidationTest() throws Exception {
        // 1. STAFF gọi phân công -> 403
        AssignTicketRequest assignToTech = new AssignTicketRequest(techUser.getId());
        mockMvc.perform(patch("/api/tickets/" + openTicket.getId() + "/assign")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignToTech)))
                .andExpect(status().isForbidden());

        // 2. ADMIN gán nhầm cho user có role STAFF -> 400 Bad Request
        AssignTicketRequest assignToStaff = new AssignTicketRequest(staffUser.getId());
        mockMvc.perform(patch("/api/tickets/" + openTicket.getId() + "/assign")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignToStaff)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("BAD_REQUEST")));

        // 3. ADMIN gán đúng cho TECHNICIAN -> 200 OK, giữ nguyên status OPEN
        mockMvc.perform(patch("/api/tickets/" + openTicket.getId() + "/assign")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignToTech)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.assigned_to.email", is("tech1@techbyte.vn")));
    }

    @Test
    @DisplayName("US-2.2 & US-2.3: Chuyển trạng thái hợp lệ/không hợp lệ (400), lưu lịch sử Transaction và bình luận")
    void statusTransitionHistoryAndCommentTest() throws Exception {
        // Gán ticket cho techUser
        openTicket.setAssignedTo(techUser);
        ticketRepository.save(openTicket);

        // 1. Cố tình hủy ngang OPEN -> CLOSED -> Bị chặn 400 Bad Request
        mockMvc.perform(patch("/api/tickets/" + openTicket.getId() + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + techToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateStatusRequest(TicketStatus.CLOSED))))
                .andExpect(status().isBadRequest());

        // 2. Chuyển hợp lệ: OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED
        mockMvc.perform(patch("/api/tickets/" + openTicket.getId() + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + techToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateStatusRequest(TicketStatus.IN_PROGRESS))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        mockMvc.perform(patch("/api/tickets/" + openTicket.getId() + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + techToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateStatusRequest(TicketStatus.RESOLVED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")));

        // 3. Thêm bình luận từ TECHNICIAN
        mockMvc.perform(post("/api/tickets/" + openTicket.getId() + "/comments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + techToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCommentRequest("Đã cấu hình lại VPN profile"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author.email", is("tech1@techbyte.vn")))
                .andExpect(jsonPath("$.content", is("Đã cấu hình lại VPN profile")));

        // 4. Kiểm tra chi tiết ticket có đủ 2 dòng lịch sử trạng thái và 1 bình luận
        mockMvc.perform(get("/api/tickets/" + openTicket.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.history", hasSize(2)))
                .andExpect(jsonPath("$.history[0].old_status", is("OPEN")))
                .andExpect(jsonPath("$.history[0].new_status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.history[1].old_status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.history[1].new_status", is("RESOLVED")));
    }

    @Test
    @DisplayName("US-2.4: Sửa và xóa ticket tuân thủ đúng chính sách trạng thái OPEN")
    void updateAndDeletePolicyTest() throws Exception {
        // 1. STAFF sửa ticket của mình khi còn OPEN -> 200 OK
        UpdateTicketRequest updateReq = new UpdateTicketRequest("Sự cố VPN khẩn", "Mô tả đã cập nhật", TicketPriority.URGENT);
        mockMvc.perform(patch("/api/tickets/" + openTicket.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Sự cố VPN khẩn")))
                .andExpect(jsonPath("$.priority", is("URGENT")));

        // 2. Khi ticket đã sang IN_PROGRESS: STAFF sửa bị chặn 400, ADMIN xóa cũng bị chặn 400
        openTicket.setStatus(TicketStatus.IN_PROGRESS);
        ticketRepository.save(openTicket);

        mockMvc.perform(patch("/api/tickets/" + openTicket.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/tickets/" + openTicket.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isBadRequest());

        // 3. Nếu ticket đang OPEN thì ADMIN xóa thành công -> 200 OK
        openTicket.setStatus(TicketStatus.OPEN);
        ticketRepository.save(openTicket);

        mockMvc.perform(delete("/api/tickets/" + openTicket.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("US-2.5 & US-2.6: Dashboard tính completed = resolved + closed từ DB thật; Health & Readiness trả 200")
    void dashboardAndHealthProbesTest() throws Exception {
        // Tạo thêm 1 ticket RESOLVED và 1 ticket CLOSED
        Ticket tResolved = new Ticket();
        tResolved.setCode("TCK-0002");
        tResolved.setTitle("Ticket đã xử lý");
        tResolved.setDescription("Mô tả");
        tResolved.setStatus(TicketStatus.RESOLVED);
        tResolved.setCreatedBy(staffUser);
        ticketRepository.save(tResolved);

        Ticket tClosed = new Ticket();
        tClosed.setCode("TCK-0003");
        tClosed.setTitle("Ticket đã đóng");
        tClosed.setDescription("Mô tả");
        tClosed.setStatus(TicketStatus.CLOSED);
        tClosed.setCreatedBy(staffUser);
        ticketRepository.save(tClosed);

        // Tổng cộng: 1 OPEN + 1 RESOLVED + 1 CLOSED => total = 3, completed = 2
        mockMvc.perform(get("/api/dashboard/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(3)))
                .andExpect(jsonPath("$.open", is(1)))
                .andExpect(jsonPath("$.in_progress", is(0)))
                .andExpect(jsonPath("$.resolved", is(1)))
                .andExpect(jsonPath("$.closed", is(1)))
                .andExpect(jsonPath("$.completed", is(2)));

        // Kiểm tra Health & Readiness ở cả /health, /api/health, /ready, /api/ready
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));

        mockMvc.perform(get("/api/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("READY")))
                .andExpect(jsonPath("$.database", is("UP")));
    }
}

