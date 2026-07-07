package com.recruitease.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitease.entity.StudentLogin;

public interface StudentLoginRepository
        extends JpaRepository<StudentLogin, Long> {

    StudentLogin findByUsernameAndPassword(
            String username,
            String password
    );

    StudentLogin findByStudentId(Long studentId);

    StudentLogin findByUsernameIgnoreCase(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);
}