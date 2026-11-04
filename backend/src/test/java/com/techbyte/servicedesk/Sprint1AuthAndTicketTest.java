package com.techbyte.servicedesk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techbyte.servicedesk.domain.entity.Ticket;
import com.techbyte.servicedesk.domain.entity.User;
import com.techbyte.servicedesk.domain.enums.Role;
import com.techbyte.servicedesk.domain.enums.TicketPriority;
import com.techbyte.servicedesk.domain.enums.TicketStatus;
import com.techbyte.servicedesk.dto.CreateTicketRequest;
import com.techbyte.servicedesk.dto.LoginRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Sprint1AuthAndTicketTest {

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
    private User staffA;
    private User staffB;

    private String adminToken;
    private String techToken;
    private String staffAToken;

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
        staffA = userRepository.save(new User(
                "staff1@techbyte.vn",
                passwordEncoder.encode("Staff@123"),
                "Nhân Viên A",
                Role.STAFF
        ));
        staffB = userRepository.save(new User(
                "staff2@techbyte.vn",
                passwordEncoder.encode("Staff@123"),
                "Nhân Viên B",
                Role.STAFF
        ));

        adminToken = jwtTokenProvider.generateToken(CustomUserPrincipal.fromUser(adminUser));
        techToken = jwtTokenProvider.generateToken(CustomUserPrincipal.fromUser(techUser));
        staffAToken = jwtTokenProvider.generateToken(CustomUserPrincipal.fromUser(staffA));
    }

    @Test
    @DisplayName("US-1.1: Đăng nhập đúng trả JWT và ẩn password; sai mật khẩu hoặc token hết hạn trả 401")
    void loginAndTokenValidationTest() throws Exception {
        LoginRequest validLogin = new LoginRequest("staff1@techbyte.vn", "Staff@123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(emptyOrNullString())))
                .andExpect(jsonPath("$.user.email", is("staff1@techbyte.vn")))
                .andExpect(jsonPath("$.user.role", is("STAFF")))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.user.password_hash").doesNotExist());

        LoginRequest wrongLogin = new LoginRequest("staff1@techbyte.vn", "WrongPass");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));

        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());

        String expiredToken = jwtTokenProvider.generateTokenWithCustomTtl(
                CustomUserPrincipal.fromUser(staffA), -10_000L);
        mockMvc.perform(get("/api/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("US-1.2: Tạo ticket kiểm tra validation (thiếu tiêu đề, mô tả > 2000 ký tự trả 400; TECHNICIAN bị chặn 403)")
    void createTicketValidationAndRoleTest() throws Exception {
        CreateTicketRequest emptyTitle = new CreateTicketRequest("", "Mô tả sự cố hợp lệ", TicketPriority.HIGH);
        mockMvc.perform(post("/api/tickets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + staffAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyTitle)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));

        String tooLongDesc = "a".repeat(2005);
        CreateTicketRequest longDescReq = new CreateTicketRequest("Lỗi server", tooLongDesc, TicketPriority.MEDIUM);
        mockMvc.perform(post("/api/tickets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + staffAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(longDescReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));

        CreateTicketRequest validReq = new CreateTicketRequest("Lỗi mạng nội bộ", "Không ping được gateway", TicketPriority.HIGH);
        mockMvc.perform(post("/api/tickets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + techToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/tickets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + staffAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", startsWith("TCK-")))
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.priority", is("HIGH")))
                .andExpect(jsonPath("$.created_by.email", is("staff1@techbyte.vn")));
    }

    @Test
    @DisplayName("US-1.3 & US-1.4: Phân quyền xem danh sách/chi tiết ticket theo 3 Roles và chống IDOR (403/404)")
    void ticketScopeAndAntiIdorTest() throws Exception {
        Ticket t1 = new Ticket();
        t1.setCode("TCK-0001");
        t1.setTitle("Lỗi thanh toán VNPay");
        t1.setDescription("Khách hàng bị trừ tiền nhưng đơn chưa cập nhật");
        t1.setPriority(TicketPriority.URGENT);
        t1.setStatus(TicketStatus.IN_PROGRESS);
        t1.setCreatedBy(staffA);
        t1.setAssignedTo(techUser);
        t1 = ticketRepository.save(t1);

        Ticket t2 = new Ticket();
        t2.setCode("TCK-0002");
        t2.setTitle("Cấp lại mật khẩu email");
        t2.setDescription("Nhân viên mới quên mật khẩu");
        t2.setPriority(TicketPriority.LOW);
        t2.setStatus(TicketStatus.OPEN);
        t2.setCreatedBy(staffB);
        t2 = ticketRepository.save(t2);

        mockMvc.perform(get("/api/tickets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + staffAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.items[0].code", is("TCK-0001")));

        mockMvc.perform(get("/api/tickets/" + t2.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + staffAToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));

        mockMvc.perform(get("/api/tickets/" + t1.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + techToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("TCK-0001")));

        mockMvc.perform(get("/api/tickets/" + t2.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + techToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/tickets")
                        .param("search", "VNPay")
                        .param("status", "IN_PROGRESS")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.items[0].code", is("TCK-0001")));

        mockMvc.perform(get("/api/tickets/99999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("NOT_FOUND")));
    }
}
