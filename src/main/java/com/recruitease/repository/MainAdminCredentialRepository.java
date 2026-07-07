package com.recruitease.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitease.entity.MainAdminCredential;

public interface MainAdminCredentialRepository
        extends JpaRepository<MainAdminCredential, Long> {

    Optional<MainAdminCredential> findTopByUsernameIgnoreCase(
            String username
    );

    Optional<MainAdminCredential>
    findTopByUsernameIgnoreCaseAndEmailIgnoreCase(
            String username,
            String email
    );
}