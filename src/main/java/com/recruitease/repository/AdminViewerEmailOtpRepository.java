
package com.recruitease.repository;

import com.recruitease.entity.AdminViewerEmailOtp;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface AdminViewerEmailOtpRepository
        extends JpaRepository<AdminViewerEmailOtp, Long> {

    Optional<AdminViewerEmailOtp> findByEmail(String email);

    boolean existsByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT otp
        FROM AdminViewerEmailOtp otp
        WHERE otp.email = :email
    """)
    Optional<AdminViewerEmailOtp> findByEmailForUpdate(
            @Param("email") String email
    );
}
