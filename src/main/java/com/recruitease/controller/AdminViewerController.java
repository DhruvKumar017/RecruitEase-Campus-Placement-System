
package com.recruitease.controller;

import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitease.service.AdminViewerService;
import com.recruitease.entity.AdminViewerCredential;
import com.recruitease.repository.AdminViewerCredentialRepository;

@RestController
@RequestMapping("/api/admin-viewer")
public class AdminViewerController {

private final AdminViewerService viewerService;
private final AdminViewerCredentialRepository credentialRepository;

public AdminViewerController(
        AdminViewerService viewerService,
        AdminViewerCredentialRepository credentialRepository
) {
    this.viewerService = viewerService;
    this.credentialRepository = credentialRepository;
}

@PostMapping("/generate")
public ResponseEntity<?> generate(
        Authentication authentication,
        @RequestBody Map<String, String> request
) {

        if (authentication == null
                || !authentication.getAuthorities().stream()
                    .anyMatch(authority ->
                        "ROLE_MAIN_ADMIN".equals(
                            authority.getAuthority()
                        )
                    )) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                        "message",
                        "Only Main Admin can generate viewer credentials."
                    ));
        }

String email = request.get("email");

try {
    return ResponseEntity.ok(
            viewerService.generateCredentials(email)
    );
} catch (IllegalArgumentException exception) {
    return ResponseEntity.badRequest().body(
            Map.of("message", exception.getMessage())
    );
} catch (IllegalStateException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(
            Map.of("message", exception.getMessage())
    );
}
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> request
    ) {

        try {

            String viewerId = request.get("viewerId");
            String password = request.get("password");

            String token = viewerService.login(
                    viewerId,
                    password
            );

return ResponseEntity.ok(Map.of(
    "token", token,
    "role", "DEMO_VIEWER",
    "viewerId", viewerId.trim(),
    "expiresIn", "30 minutes"
));

        } catch (IllegalArgumentException exception) {

            return ResponseEntity.status(
                    HttpStatus.UNAUTHORIZED
            ).body(Map.of(
                "message",
                "Invalid or expired viewer credentials."
            ));
        }
    }
    @GetMapping("/history")
public ResponseEntity<?> getViewerHistory(
        Authentication authentication
) {

    boolean isAdmin = authentication != null
            && authentication.getAuthorities().stream()
            .anyMatch(authority ->
                    "ROLE_MAIN_ADMIN".equals(authority.getAuthority())
                    || "ROLE_ADMIN".equals(authority.getAuthority())
            );

    if (!isAdmin) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "message",
                        "Only Admin can view login history."
                ));
    }

    List<AdminViewerCredential> credentials =
            credentialRepository
                    .findByLoginAtIsNotNullOrderByLoginAtDesc();

    List<Map<String, Object>> history = new ArrayList<>();

    for (AdminViewerCredential credential : credentials) {

        Map<String, Object> record = new LinkedHashMap<>();

        record.put("email", credential.getViewerEmail());
        record.put("viewerId", credential.getViewerId());
        record.put("password", "••••••••");
        record.put("loginAt", credential.getLoginAt().toString());

        history.add(record);
    }

    return ResponseEntity.ok(history);
}
}
