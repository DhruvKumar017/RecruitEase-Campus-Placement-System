package com.recruitease.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitease.entity.AdminPasswordResetOtp;

public interface AdminPasswordResetOtpRepository
        extends JpaRepository<AdminPasswordResetOtp, Long> {

    Optional<AdminPasswordResetOtp>
    findTopByUsernameIgnoreCaseAndEmailIgnoreCaseOrderByIdDesc(
            String username,
            String email
    );
}