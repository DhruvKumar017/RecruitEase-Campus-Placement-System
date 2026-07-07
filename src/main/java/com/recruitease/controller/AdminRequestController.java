package com.recruitease.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.recruitease.entity.AdminRequest;
import com.recruitease.repository.AdminRequestRepository;

@RestController
public class AdminRequestController {

    private final AdminRequestRepository adminRequestRepository;

    public AdminRequestController(
            AdminRequestRepository adminRequestRepository
    ) {
        this.adminRequestRepository = adminRequestRepository;
    }

    /* New admin submits request */

    @PostMapping("/api/admin-requests")
    public ResponseEntity<?> createAdminRequest(
            @RequestBody AdminRequest request
    ) {
        if (request.getFullName() == null ||
            request.getFullName().trim().isEmpty() ||
            request.getEmail() == null ||
            request.getEmail().trim().isEmpty() ||
            request.getRequestedUsername() == null ||
            request.getRequestedUsername().trim().isEmpty() ||
            request.getRequestedPassword() == null ||
            request.getRequestedPassword().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Please fill all required admin request details.");
        }

        if (adminRequestRepository.existsByRequestedUsernameIgnoreCase(
                request.getRequestedUsername().trim()
        )) {
            return ResponseEntity
                    .badRequest()
                    .body("This username request already exists.");
        }

        if (adminRequestRepository.existsByEmailIgnoreCase(
                request.getEmail().trim()
        )) {
            return ResponseEntity
                    .badRequest()
                    .body("This email request already exists.");
        }

        request.setFullName(request.getFullName().trim());
        request.setEmail(request.getEmail().trim());
        request.setRequestedUsername(
                request.getRequestedUsername().trim()
        );

        request.setStatus("Pending");

        AdminRequest savedRequest =
                adminRequestRepository.save(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedRequest);
    }

    /* Main admin sees all requests */

    @GetMapping("/api/admin-requests")
    public List<AdminRequest> getAllAdminRequests() {
        return adminRequestRepository.findAll();
    }

    /* Main admin sees only pending requests */

    @GetMapping("/api/admin-requests/pending")
    public List<AdminRequest> getPendingAdminRequests() {
        return adminRequestRepository
                .findByStatusIgnoreCase("Pending");
    }

    /* Main admin approves request */

    @PutMapping("/api/admin-requests/{id}/approve")
    public ResponseEntity<?> approveAdminRequest(
            @PathVariable Long id
    ) {
        AdminRequest request = adminRequestRepository
                .findById(id)
                .orElse(null);

        if (request == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Admin request not found.");
        }

        if (!"Pending".equalsIgnoreCase(request.getStatus())) {
            return ResponseEntity
                    .badRequest()
                    .body("Only pending requests can be approved.");
        }

        request.setStatus("Approved");

        return ResponseEntity.ok(
                adminRequestRepository.save(request)
        );
    }

    /* Main admin rejects request */

    @PutMapping("/api/admin-requests/{id}/reject")
    public ResponseEntity<?> rejectAdminRequest(
            @PathVariable Long id
    ) {
        AdminRequest request = adminRequestRepository
                .findById(id)
                .orElse(null);

        if (request == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Admin request not found.");
        }

        if (!"Pending".equalsIgnoreCase(request.getStatus())) {
            return ResponseEntity
                    .badRequest()
                    .body("Only pending requests can be rejected.");
        }

        request.setStatus("Rejected");

        return ResponseEntity.ok(
                adminRequestRepository.save(request)
        );
    }
}