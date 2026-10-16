
package com.recruitease.repository;

import java.util.Optional;
import java.util.List;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitease.entity.AdminViewerCredential;

@Repository
public interface AdminViewerCredentialRepository
        extends JpaRepository<AdminViewerCredential, Long> {

    // Find viewer credentials with database locking
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM AdminViewerCredential v WHERE v.viewerId = :viewerId")
    Optional<AdminViewerCredential> findByViewerId(
            @Param("viewerId") String viewerId
    );

    // Check whether Viewer ID already exists
boolean existsByViewerId(String viewerId);
boolean existsByViewerEmail(String viewerEmail);

// Fetch successful viewer logins, newest first
List<AdminViewerCredential> findByLoginAtIsNotNullOrderByLoginAtDesc();

}
