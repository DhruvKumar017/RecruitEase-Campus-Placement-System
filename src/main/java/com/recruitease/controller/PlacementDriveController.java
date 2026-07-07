package com.recruitease.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitease.entity.Company;
import com.recruitease.entity.PlacementDrive;
import com.recruitease.repository.CompanyRepository;
import com.recruitease.repository.PlacementDriveRepository;

@RestController
@RequestMapping("/api/drives")
@CrossOrigin("*")
public class PlacementDriveController {

    private final PlacementDriveRepository placementDriveRepository;
    private final CompanyRepository companyRepository;

    public PlacementDriveController(
            PlacementDriveRepository placementDriveRepository,
            CompanyRepository companyRepository
    ) {
        this.placementDriveRepository = placementDriveRepository;
        this.companyRepository = companyRepository;
    }

    @GetMapping
    public List<PlacementDrive> getAllDrives() {
        return placementDriveRepository.findAll();
    }

    @GetMapping("/{id}")
    public PlacementDrive getDriveById(@PathVariable Long id) {
        return placementDriveRepository.findById(id).orElse(null);
    }

    @PostMapping
    public ResponseEntity<?> createDrive(@RequestBody PlacementDrive drive) {

        if (drive.getCompanyName() == null || drive.getCompanyName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Please select a company first.");
        }

        Company company = companyRepository
                .findByCompanyNameIgnoreCase(drive.getCompanyName().trim())
                .orElse(null);

        if (company == null) {
            return ResponseEntity.badRequest()
                    .body("Company does not exist. Add this company first from All Companies.");
        }

        drive.setCompanyName(company.getCompanyName());

        return ResponseEntity.ok(
                placementDriveRepository.save(drive)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDrive(
            @PathVariable Long id,
            @RequestBody PlacementDrive updatedDrive
    ) {

        PlacementDrive drive = placementDriveRepository.findById(id).orElse(null);

        if (drive == null) {
            return ResponseEntity.notFound().build();
        }

        if (updatedDrive.getCompanyName() == null ||
                updatedDrive.getCompanyName().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Please select a company first.");
        }

        Company company = companyRepository
                .findByCompanyNameIgnoreCase(updatedDrive.getCompanyName().trim())
                .orElse(null);

        if (company == null) {
            return ResponseEntity.badRequest()
                    .body("Company does not exist. Add this company first from All Companies.");
        }

        drive.setCompanyName(company.getCompanyName());
        drive.setJobRole(updatedDrive.getJobRole());
        drive.setPackageLpa(updatedDrive.getPackageLpa());
        drive.setDriveDate(updatedDrive.getDriveDate());
        drive.setLastDate(updatedDrive.getLastDate());
        drive.setMode(updatedDrive.getMode());
        drive.setLocation(updatedDrive.getLocation());
        drive.setBranch(updatedDrive.getBranch());
        drive.setMinCgpa(updatedDrive.getMinCgpa());
        drive.setSkills(updatedDrive.getSkills());
        drive.setVacancy(updatedDrive.getVacancy());
        drive.setStatus(updatedDrive.getStatus());

        return ResponseEntity.ok(
                placementDriveRepository.save(drive)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDrive(@PathVariable Long id) {

        if (!placementDriveRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        placementDriveRepository.deleteById(id);

        return ResponseEntity.ok("Placement drive deleted successfully.");
    }
}