package com.techbyte.servicedesk.controller;

import com.techbyte.servicedesk.dto.DashboardSummaryResponse;
import com.techbyte.servicedesk.security.CustomUserPrincipal;
import com.techbyte.servicedesk.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(dashboardService.getSummary(principal));
    }
}

