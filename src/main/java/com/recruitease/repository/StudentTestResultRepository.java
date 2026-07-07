package com.recruitease.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitease.entity.StudentTestResult;

public interface StudentTestResultRepository
        extends JpaRepository<StudentTestResult, Long> {

    List<StudentTestResult> findByStudentId(Long studentId);

    Optional<StudentTestResult> findByStudentIdAndApplicationId(
            Long studentId,
            Long applicationId
    );

    Optional<StudentTestResult>
            findTopByApplicationIdOrderBySubmittedAtDesc(Long applicationId);
}