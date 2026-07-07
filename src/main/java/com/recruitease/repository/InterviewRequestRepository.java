package com.recruitease.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitease.entity.InterviewRequest;

public interface InterviewRequestRepository
        extends JpaRepository<InterviewRequest, Long> {

    List<InterviewRequest> findByStudentId(Long studentId);

    List<InterviewRequest> findByCompanyId(Long companyId);

    Optional<InterviewRequest> findByStudentIdAndCompanyId(
            Long studentId,
            Long companyId
    );

    boolean existsByStudentIdAndCompanyId(
            Long studentId,
            Long companyId
    );

    /* Specific application interview request */
    Optional<InterviewRequest> findByApplicationId(Long applicationId);

    boolean existsByApplicationId(Long applicationId);
}