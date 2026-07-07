package com.recruitease.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitease.entity.AdminNotificationRead;

public interface AdminNotificationReadRepository
        extends JpaRepository<AdminNotificationRead, Long> {

    boolean existsByAdminUsernameAndNotificationKey(
            String adminUsername,
            String notificationKey
    );

    Optional<AdminNotificationRead>
    findByAdminUsernameAndNotificationKey(
            String adminUsername,
            String notificationKey
    );

    List<AdminNotificationRead> findByAdminUsername(
            String adminUsername
    );
}