package com.recruitease.security;

import java.io.IOException;
import java.util.List;

import com.recruitease.entity.AdminRequest;
import com.recruitease.repository.AdminRequestRepository;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

private final JwtService jwtService;
private final AdminRequestRepository adminRequestRepository;

public JwtAuthenticationFilter(
        JwtService jwtService,
        AdminRequestRepository adminRequestRepository
) {
    this.jwtService = jwtService;
    this.adminRequestRepository = adminRequestRepository;
}

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader = request.getHeader(
                HttpHeaders.AUTHORIZATION
        );

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7).trim();

        try {
if (!jwtService.isTokenValid(token)) {
    SecurityContextHolder.clearContext();

    response.sendError(
            HttpServletResponse.SC_UNAUTHORIZED,
            "Invalid or expired authentication token."
    );
    return;
}

if (SecurityContextHolder.getContext()
        .getAuthentication() == null) {

                String username =
                        jwtService.extractUsername(token);

String role =
        jwtService.extractRole(token);

// Check whether the approved admin still has access.
if ("ADMIN".equals(role)) {

    AdminRequest admin = adminRequestRepository
            .findByRequestedUsernameIgnoreCase(username)
            .orElse(null);

    if (admin == null ||
            !"Approved".equalsIgnoreCase(admin.getStatus())) {

        SecurityContextHolder.clearContext();

        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Admin access has been revoked or is no longer approved."
        );
        return;
    }
}
                        // Demo Viewer must never access real application APIs.
if ("DEMO_VIEWER".equals(role)) {

    String path = request.getServletPath();

boolean allowedDemoApi =
        "GET".equals(request.getMethod())
        && path.equals("/api/demo-viewer/dashboard");

    if (!allowedDemoApi) {

        SecurityContextHolder.clearContext();

        response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Demo Viewer cannot access this API."
        );
        return;
    }
}

                if (username != null &&
                        !username.isBlank() &&
                        role != null &&
                        !role.isBlank()) {

                    SimpleGrantedAuthority authority =
                            new SimpleGrantedAuthority(
                                    "ROLE_" + role.trim()
                            );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    List.of(authority)
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }

            }

} catch (Exception error) {
    SecurityContextHolder.clearContext();

    response.sendError(
            HttpServletResponse.SC_UNAUTHORIZED,
            "Invalid or expired authentication token."
    );
    return;
}

filterChain.doFilter(request, response);
    }
}