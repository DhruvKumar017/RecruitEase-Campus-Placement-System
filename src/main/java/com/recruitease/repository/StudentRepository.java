package com.recruitease.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitease.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByBlocked(boolean blocked);

    List<Student> findByDeleted(boolean deleted);

    List<Student> findByBlockedAndDeleted(boolean blocked, boolean deleted);

    List<Student> findByNameContainingIgnoreCase(String name);

    List<Student> findByRollNumberContainingIgnoreCase(String rollNumber);

    boolean existsByEmail(String email);

    boolean existsByMobile(String mobile);

    Student findByEmailIgnoreCase(String email);
}