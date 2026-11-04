package com.techbyte.servicedesk.controller;

import com.techbyte.servicedesk.dto.LoginRequest;
import com.techbyte.servicedesk.dto.LoginResponse;
import com.techbyte.servicedesk.dto.UserSummaryDto;
import com.techbyte.servicedesk.security.CustomUserPrincipal;
import com.techbyte.servicedesk.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummaryDto> getCurrentUser(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(UserSummaryDto.fromPrincipal(principal));
    }
}

