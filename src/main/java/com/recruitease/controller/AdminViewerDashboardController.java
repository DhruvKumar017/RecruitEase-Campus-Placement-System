
package com.recruitease.controller;

import com.recruitease.repository.StudentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin-viewer/dashboard")
public class AdminViewerDashboardController {

    private final StudentRepository studentRepository;

    public AdminViewerDashboardController(
            StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public ResponseEntity<?> getDashboard(Authentication authentication) {

        boolean isViewer = authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_VIEWER".equals(authority.getAuthority()));

        if (!isViewer) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Viewer access required."));
        }

        long totalStudents =
                studentRepository.findByDeleted(false).size();

        return ResponseEntity.ok(
                Map.of("totalStudents", totalStudents)
        );
    }
}
