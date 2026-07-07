package com.recruitease.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitease.entity.PasswordResetOtp;

public interface PasswordResetOtpRepository
        extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp>
    findTopByStudentIdAndEmailIgnoreCaseOrderByIdDesc(
            Long studentId,
            String email
    );
}