package com.recruitease.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitease.entity.AdminRequest;

public interface AdminRequestRepository
        extends JpaRepository<AdminRequest, Long> {

    List<AdminRequest> findByStatusIgnoreCase(String status);

    boolean existsByRequestedUsernameIgnoreCase(String requestedUsername);

    boolean existsByEmailIgnoreCase(String email);

    Optional<AdminRequest> findByRequestedUsernameIgnoreCase(
            String requestedUsername
    );

    Optional<AdminRequest>
    findByRequestedUsernameIgnoreCaseAndEmailIgnoreCase(
            String requestedUsername,
            String email
    );
}