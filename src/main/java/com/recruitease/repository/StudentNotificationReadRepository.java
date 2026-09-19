package com.recruitease.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitease.entity.StudentNotificationRead;

public interface StudentNotificationReadRepository
        extends JpaRepository<StudentNotificationRead, Long> {

    Optional<StudentNotificationRead>
            findByStudentIdAndNotificationKey(
                    Long studentId,
                    String notificationKey
            );
}