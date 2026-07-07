package com.recruitease.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitease.entity.Company;
import com.recruitease.repository.CompanyRepository;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyRepository companyRepository;

    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    /* ================= GET ALL COMPANIES ================= */

    @GetMapping
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    /* ================= GET COMPANY BY ID ================= */

    @GetMapping("/{id}")
    public ResponseEntity<?> getCompanyById(@PathVariable Long id) {

        Company company = companyRepository.findById(id).orElse(null);

        if (company == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(company);
    }

    /* ================= ADD COMPANY ================= */

    @PostMapping("/add")
    public ResponseEntity<?> addCompany(@RequestBody Company company) {

        if (company.getCompanyName() == null ||
                company.getCompanyName().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Company name is required.");
        }

        if (company.getMinCgpa() == null ||
                company.getMinCgpa() < 0 ||
                company.getMinCgpa() > 10) {

            return ResponseEntity.badRequest()
                    .body("Minimum CGPA must be between 0 and 10.");
        }

        if (company.getRequiredSkills() == null ||
                company.getRequiredSkills().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Required skills are required.");
        }

        String companyName = company.getCompanyName().trim();

        Optional<Company> existingCompany =
                companyRepository.findByCompanyNameIgnoreCase(companyName);

        if (existingCompany.isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Company already exists.");
        }

        company.setCompanyName(companyName);
        company.setRequiredSkills(company.getRequiredSkills().trim());

        if (company.getAptitudeCutoff() != null &&
                company.getAptitudeCutoff() < 0) {

            return ResponseEntity.badRequest()
                    .body("Aptitude cutoff cannot be negative.");
        }

        Company savedCompany = companyRepository.save(company);

        return ResponseEntity.ok(savedCompany);
    }

    /* ================= UPDATE COMPANY ================= */

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCompany(
            @PathVariable Long id,
            @RequestBody Company updatedCompany
    ) {

        Company company = companyRepository.findById(id).orElse(null);

        if (company == null) {
            return ResponseEntity.notFound().build();
        }

        if (updatedCompany.getCompanyName() == null ||
                updatedCompany.getCompanyName().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Company name is required.");
        }

        if (updatedCompany.getMinCgpa() == null ||
                updatedCompany.getMinCgpa() < 0 ||
                updatedCompany.getMinCgpa() > 10) {

            return ResponseEntity.badRequest()
                    .body("Minimum CGPA must be between 0 and 10.");
        }

        if (updatedCompany.getRequiredSkills() == null ||
                updatedCompany.getRequiredSkills().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Required skills are required.");
        }

        String newCompanyName = updatedCompany.getCompanyName().trim();

        Optional<Company> sameNameCompany =
                companyRepository.findByCompanyNameIgnoreCase(newCompanyName);

        if (sameNameCompany.isPresent() &&
                !sameNameCompany.get().getId().equals(id)) {

            return ResponseEntity.badRequest()
                    .body("Another company already exists with this name.");
        }

        if (updatedCompany.getAptitudeCutoff() != null &&
                updatedCompany.getAptitudeCutoff() < 0) {

            return ResponseEntity.badRequest()
                    .body("Aptitude cutoff cannot be negative.");
        }

        company.setCompanyName(newCompanyName);
        company.setMinCgpa(updatedCompany.getMinCgpa());
        company.setRequiredSkills(updatedCompany.getRequiredSkills().trim());
        company.setAptitudeCutoff(updatedCompany.getAptitudeCutoff());

        return ResponseEntity.ok(companyRepository.save(company));
    }

    /* ================= DELETE COMPANY ================= */

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable Long id) {

        if (!companyRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        companyRepository.deleteById(id);

        return ResponseEntity.ok("Company deleted successfully.");
    }
}