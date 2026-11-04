package com.techbyte.servicedesk.service;

import com.techbyte.servicedesk.dto.LoginRequest;
import com.techbyte.servicedesk.dto.LoginResponse;
import com.techbyte.servicedesk.dto.UserSummaryDto;
import com.techbyte.servicedesk.security.CustomUserPrincipal;
import com.techbyte.servicedesk.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().trim(), request.getPassword())
        );

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(principal);
        return new LoginResponse(token, UserSummaryDto.fromPrincipal(principal));
    }
}

