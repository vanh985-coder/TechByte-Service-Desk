package com.techbyte.servicedesk;

import com.techbyte.servicedesk.security.CustomUserDetailsService;
import com.techbyte.servicedesk.security.JwtAuthenticationFilter;
import com.techbyte.servicedesk.security.JwtTokenProvider;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    private final JwtTokenProvider tokens = mock(JwtTokenProvider.class);
    private final CustomUserDetailsService users = mock(CustomUserDetailsService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokens, users);

    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    void deletedUserIsUnauthenticatedInsteadOfCausingServerError() throws Exception {
        when(tokens.validateToken("test-token")).thenReturn(true);
        when(tokens.getEmailFromToken("test-token")).thenReturn("deleted@example.test");
        when(users.loadUserByUsername("deleted@example.test"))
                .thenThrow(new UsernameNotFoundException("User was removed"));
        assertContinuesUnauthenticated();
    }

    @Test
    void tokenInvalidatedDuringParsingDoesNotEscapeTheFilter() throws Exception {
        when(tokens.validateToken("test-token")).thenReturn(true);
        when(tokens.getEmailFromToken("test-token")).thenThrow(new MalformedJwtException("Invalid token"));
        assertContinuesUnauthenticated();
        verifyNoInteractions(users);
    }

    private void assertContinuesUnauthenticated() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/me");
        request.addHeader("Authorization", "Bearer test-token");
        var chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        assertSame(request, chain.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
