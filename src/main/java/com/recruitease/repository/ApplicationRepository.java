package com.recruitease.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitease.entity.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByStudentId(Long studentId);

    /* Existing company-wise application check */
    Optional<Application> findByStudentIdAndCompanyId(
            Long studentId,
            Long companyId
    );

    /* New: exact placement drive application check */
    Optional<Application> findByStudentIdAndDriveId(
            Long studentId,
            Long driveId
    );
}