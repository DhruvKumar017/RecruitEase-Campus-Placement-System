package com.recruitease.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitease.dto.StudentPlacementDriveDTO;
import com.recruitease.entity.Application;
import com.recruitease.entity.Company;
import com.recruitease.entity.PlacementDrive;
import com.recruitease.entity.Student;
import com.recruitease.repository.ApplicationRepository;
import com.recruitease.repository.CompanyRepository;
import com.recruitease.repository.PlacementDriveRepository;
import com.recruitease.repository.StudentRepository;

@RestController
public class StudentPlacementDriveController {

    private final PlacementDriveRepository placementDriveRepository;
    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;

    public StudentPlacementDriveController(
            PlacementDriveRepository placementDriveRepository,
            StudentRepository studentRepository,
            ApplicationRepository applicationRepository,
            CompanyRepository companyRepository
    ) {
        this.placementDriveRepository = placementDriveRepository;
        this.studentRepository = studentRepository;
        this.applicationRepository = applicationRepository;
        this.companyRepository = companyRepository;
    }

    @GetMapping("/api/student-placement-drives/{studentId}")
    public ResponseEntity<?> getStudentPlacementDrives(
            @PathVariable Long studentId
    ) {
        Student student = studentRepository.findById(studentId).orElse(null);

        if (student == null || student.isDeleted() || student.isBlocked()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Student account is not active.")
            );
        }

        List<StudentPlacementDriveDTO> drives =
                placementDriveRepository.findAll()
                        .stream()
                        .map(drive -> buildDriveDto(student, drive))
                        .toList();

        return ResponseEntity.ok(drives);
    }

    @PostMapping("/api/student-placement-drives/{studentId}/{driveId}/apply")
    public ResponseEntity<?> applyForPlacementDrive(
            @PathVariable Long studentId,
            @PathVariable Long driveId
    ) {
        Student student = studentRepository.findById(studentId).orElse(null);

        if (student == null || student.isDeleted() || student.isBlocked()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Student account is not active.")
            );
        }

        PlacementDrive drive =
                placementDriveRepository.findById(driveId).orElse(null);

        if (drive == null) {
            return ResponseEntity.notFound().build();
        }

        boolean alreadyApplied = applicationRepository
                .findByStudentIdAndDriveId(studentId, driveId)
                .isPresent();

        if (alreadyApplied) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "You have already applied for this drive.")
            );
        }

        /* Apply sirf Active/Open drive me hoga */
        String driveStatus = normalize(drive.getStatus());

        if (!driveStatus.equals("active") && !driveStatus.equals("open")) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "This drive is upcoming. Apply button active hone ka wait karo."
                    )
            );
        }

        String eligibilityReason = getEligibilityReason(student, drive);

        if (eligibilityReason != null) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", eligibilityReason)
            );
        }

        Company company = companyRepository
                .findByCompanyNameIgnoreCase(drive.getCompanyName())
                .orElse(null);

        if (company == null) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Company data not found for this placement drive."
                    )
            );
        }

        Application application = new Application();

        application.setStudentId(studentId);
        application.setCompanyId(company.getId());
        application.setDriveId(driveId);
        application.setCompanyName(drive.getCompanyName());
        application.setRole(drive.getJobRole());
        application.setStatus("Applied");

        Application savedApplication =
                applicationRepository.save(application);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Application submitted successfully.",
                        "applicationId",
                        savedApplication.getId()
                )
        );
    }

    private StudentPlacementDriveDTO buildDriveDto(
            Student student,
            PlacementDrive drive
    ) {
        StudentPlacementDriveDTO dto = new StudentPlacementDriveDTO();

        dto.setDriveId(drive.getId());
        dto.setCompanyName(drive.getCompanyName());
        dto.setJobRole(drive.getJobRole());
        dto.setPackageLpa(drive.getPackageLpa());
        dto.setDriveDate(drive.getDriveDate());
        dto.setLastDate(drive.getLastDate());
        dto.setMode(drive.getMode());
        dto.setLocation(drive.getLocation());
        dto.setBranch(drive.getBranch());
        dto.setMinCgpa(drive.getMinCgpa());
        dto.setSkills(drive.getSkills());
        dto.setVacancy(drive.getVacancy());
        dto.setStatus(drive.getStatus());

        boolean alreadyApplied = applicationRepository
                .findByStudentIdAndDriveId(student.getId(), drive.getId())
                .isPresent();

        dto.setAlreadyApplied(alreadyApplied);

        String eligibilityReason = getEligibilityReason(student, drive);

        String driveStatus = normalize(drive.getStatus());

        boolean activeDrive =
                driveStatus.equals("active") ||
                driveStatus.equals("open");

        boolean canApply =
                activeDrive &&
                eligibilityReason == null &&
                !alreadyApplied;

        dto.setEligible(eligibilityReason == null);
        dto.setCanApply(canApply);

        if (alreadyApplied) {
            dto.setEligibilityReason(
                    "Already applied for this drive."
            );

        } else if (!activeDrive) {
            dto.setEligibilityReason(
                    "Drive is upcoming. Apply button will start when admin marks it Active."
            );

        } else if (eligibilityReason != null) {
            dto.setEligibilityReason(eligibilityReason);

        } else {
            dto.setEligibilityReason("Eligible to apply.");
        }

        return dto;
    }

    private String getEligibilityReason(
            Student student,
            PlacementDrive drive
    ) {
        List<String> reasons = new ArrayList<>();

        if (student.getCgpa() == null) {
            reasons.add("Please update your CGPA in profile.");

        } else if (
                drive.getMinCgpa() != null &&
                student.getCgpa() < drive.getMinCgpa()
        ) {
            reasons.add(
                    "Minimum CGPA " +
                    drive.getMinCgpa() +
                    " required."
            );
        }

        if (!branchMatches(student.getBranch(), drive.getBranch())) {
            reasons.add(
                    "Your branch is not eligible for this drive."
            );
        }

        if (!skillsMatch(student.getSkills(), drive.getSkills())) {
            reasons.add(
                    "Required skills not found: " +
                    safeText(drive.getSkills(), "Not specified")
            );
        }

        if (reasons.isEmpty()) {
            return null;
        }

        return String.join(" ", reasons);
    }

    private boolean branchMatches(
            String studentBranch,
            String allowedBranches
    ) {
        String allowedText = normalize(allowedBranches);

        if (allowedText.isEmpty() || allowedText.equals("all")) {
            return true;
        }

        String studentText = normalize(studentBranch);

        if (studentText.isEmpty()) {
            return false;
        }

        for (String branch : allowedText.split("[,/|]")) {
            String cleanBranch = branch.trim();

            if (cleanBranch.isEmpty()) {
                continue;
            }

            if (
                    studentText.equals(cleanBranch) ||
                    studentText.contains(cleanBranch) ||
                    cleanBranch.contains(studentText)
            ) {
                return true;
            }
        }

        return false;
    }

    private boolean skillsMatch(
            String studentSkills,
            String requiredSkills
    ) {
        String requiredText = normalize(requiredSkills);

        if (requiredText.isEmpty()) {
            return true;
        }

        String studentText = normalize(studentSkills);

        if (studentText.isEmpty()) {
            return false;
        }

        for (String skill : requiredText.split("[,/|]")) {
            String cleanSkill = skill.trim();

            if (
                    !cleanSkill.isEmpty() &&
                    studentText.contains(cleanSkill)
            ) {
                return true;
            }
        }

        return false;
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase();
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty()
                ? fallback
                : value.trim();
    }
}