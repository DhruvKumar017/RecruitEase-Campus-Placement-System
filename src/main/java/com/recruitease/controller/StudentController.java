package com.recruitease.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.recruitease.dto.StudentEligibilityDTO;
import com.recruitease.entity.Company;
import com.recruitease.entity.Student;
import com.recruitease.entity.StudentLogin;
import com.recruitease.repository.CompanyRepository;
import com.recruitease.repository.StudentLoginRepository;
import com.recruitease.repository.StudentRepository;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private static final String UPLOAD_DIR = "uploads/resumes/";
    private static final String PHOTO_UPLOAD_DIR = "uploads/student-photos/";

    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final StudentLoginRepository studentLoginRepository;

    public StudentController(
            StudentRepository studentRepository,
            CompanyRepository companyRepository,
            StudentLoginRepository studentLoginRepository
    ) {
        this.studentRepository = studentRepository;
        this.companyRepository = companyRepository;
        this.studentLoginRepository = studentLoginRepository;
    }

    /* ================= REGISTER STUDENT ================= */

    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(@RequestBody Student student) {

        if (studentLoginRepository.existsByUsername(student.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        if (studentRepository.existsByEmail(student.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        if (studentRepository.existsByMobile(student.getMobile())) {
            return ResponseEntity.badRequest().body("Mobile number already registered");
        }

        Student savedStudent = studentRepository.save(student);

        StudentLogin login = new StudentLogin();
        login.setStudentId(savedStudent.getId());
        login.setUsername(student.getUsername());
        login.setPassword(student.getPassword());

        studentLoginRepository.save(login);

        return ResponseEntity.ok(login);
    }

    /* ================= GET ACTIVE STUDENTS ================= */

    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepository.findByDeleted(false);
    }

    /* ================= ELIGIBILITY SUMMARY ================= */

    @GetMapping("/eligibility-summary")
    public List<StudentEligibilityDTO> getEligibilitySummary() {

        List<Student> activeStudents =
                studentRepository.findByBlockedAndDeleted(false, false);

        List<Company> companies = companyRepository.findAll();

        return activeStudents.stream()
                .map(student -> {

                    List<String> eligibleCompanies = new ArrayList<>();

                    for (Company company : companies) {

                        if (student.getCgpa() == null ||
                                company.getMinCgpa() == null ||
                                student.getCgpa() < company.getMinCgpa()) {
                            continue;
                        }

                        String requiredSkills = company.getRequiredSkills();

                        /* Company ne skill requirement nahi dali */
                        if (requiredSkills == null || requiredSkills.trim().isEmpty()) {
                            eligibleCompanies.add(company.getCompanyName());
                            continue;
                        }

                        String studentSkills = student.getSkills() == null
                                ? ""
                                : student.getSkills().toLowerCase();

                        String[] requiredSkillsArray =
                                requiredSkills.toLowerCase().split(",");

                        boolean skillMatched = false;

                        for (String skill : requiredSkillsArray) {
                            if (studentSkills.contains(skill.trim())) {
                                skillMatched = true;
                                break;
                            }
                        }

                        if (skillMatched) {
                            eligibleCompanies.add(company.getCompanyName());
                        }
                    }

                    return new StudentEligibilityDTO(
                            student.getId(),
                            student.getName(),
                            student.getRollNumber(),
                            student.getBranch(),
                            student.getCgpa(),
                            student.getSkills(),
                            eligibleCompanies.size(),
                            eligibleCompanies
                    );
                })
                .toList();
    }

    /* ================= GET DELETED STUDENTS ================= */

    @GetMapping("/deleted")
    public List<Student> getDeletedStudents() {
        return studentRepository.findByDeleted(true);
    }

    /* ================= GET BLOCKED STUDENTS ================= */

    @GetMapping("/blocked")
    public List<Student> getBlockedStudents() {
        return studentRepository.findByBlocked(true);
    }

    /* ================= GET STUDENT BY ID ================= */

    @GetMapping("/{id}")
    public Student getStudentById(@PathVariable Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    /* ================= GET ELIGIBLE COMPANIES FOR ONE STUDENT ================= */

    @GetMapping("/{id}/eligible-companies")
    public List<Company> getEligibleCompanies(@PathVariable Long id) {

        Student student = studentRepository.findById(id).orElse(null);

        if (student == null ||
                student.getCgpa() == null ||
                student.getSkills() == null) {
            return List.of();
        }

        return companyRepository.findAll()
                .stream()
                .filter(company -> company.getMinCgpa() != null)
                .filter(company -> student.getCgpa() >= company.getMinCgpa())
                .filter(company -> {

                    if (company.getRequiredSkills() == null ||
                            company.getRequiredSkills().isEmpty()) {
                        return true;
                    }

                    String studentSkills = student.getSkills().toLowerCase();

                    String[] requiredSkills =
                            company.getRequiredSkills()
                                    .toLowerCase()
                                    .split(",");

                    for (String skill : requiredSkills) {
                        if (studentSkills.contains(skill.trim())) {
                            return true;
                        }
                    }

                    return false;
                })
                .toList();
    }

    /* ================= BLOCK STUDENT ================= */

    @PutMapping("/{id}/block")
    public Student blockStudent(@PathVariable Long id) {

        Student student = studentRepository.findById(id).orElse(null);

        if (student == null) {
            return null;
        }

        student.setBlocked(true);

        return studentRepository.save(student);
    }

    /* ================= UNBLOCK STUDENT ================= */

    @PutMapping("/{id}/unblock")
    public Student unblockStudent(@PathVariable Long id) {

        Student student = studentRepository.findById(id).orElse(null);

        if (student == null) {
            return null;
        }

        student.setBlocked(false);

        return studentRepository.save(student);
    }

    /* ================= SOFT DELETE STUDENT ================= */

    @PutMapping("/{id}/delete")
    public Student deleteStudent(@PathVariable Long id) {

        Student student = studentRepository.findById(id).orElse(null);

        if (student == null) {
            return null;
        }

        student.setDeleted(true);

        return studentRepository.save(student);
    }

    /* ================= RESTORE STUDENT ================= */

    @PutMapping("/{id}/restore")
    public Student restoreStudent(@PathVariable Long id) {

        Student student = studentRepository.findById(id).orElse(null);

        if (student == null) {
            return null;
        }

        student.setDeleted(false);

        return studentRepository.save(student);
    }

    /* ================= PERMANENT DELETE ================= */

    @DeleteMapping("/{id}/permanent")
    public void permanentDelete(@PathVariable Long id) {
        studentRepository.deleteById(id);
    }

    /* ================= UPDATE STUDENT ================= */

    @PutMapping("/{id}/update")
    public Student updateStudent(
            @PathVariable Long id,
            @RequestBody Student updatedStudent
    ) {

        Student student = studentRepository.findById(id).orElse(null);

        if (student == null) {
            return null;
        }

        student.setName(updatedStudent.getName());
        student.setEmail(updatedStudent.getEmail());
        student.setRollNumber(updatedStudent.getRollNumber());
        student.setBranch(updatedStudent.getBranch());
        student.setCgpa(updatedStudent.getCgpa());
        student.setSkills(updatedStudent.getSkills());
        student.setPreferredCompany(updatedStudent.getPreferredCompany());

        student.setMobile(updatedStudent.getMobile());
        student.setYear(updatedStudent.getYear());
        student.setAddress(updatedStudent.getAddress());
        student.setLinkedin(updatedStudent.getLinkedin());
        student.setGithub(updatedStudent.getGithub());

        return studentRepository.save(student);
    }

    /* ================= UPDATE STUDENT PROFILE ================= */

    @PutMapping("/{id}/update-profile")
    public Student updateStudentProfile(
            @PathVariable Long id,
            @RequestBody Student updatedStudent
    ) {

        Student student = studentRepository.findById(id).orElse(null);

        if (student == null) {
            return null;
        }

        student.setName(updatedStudent.getName());
        student.setEmail(updatedStudent.getEmail());
        student.setSkills(updatedStudent.getSkills());
        student.setPreferredCompany(updatedStudent.getPreferredCompany());

        student.setMobile(updatedStudent.getMobile());
        student.setYear(updatedStudent.getYear());
        student.setAddress(updatedStudent.getAddress());
        student.setLinkedin(updatedStudent.getLinkedin());
        student.setGithub(updatedStudent.getGithub());

        return studentRepository.save(student);
    }

    /* ================= UPLOAD RESUME ================= */

    @PostMapping("/{id}/upload-resume")
    public Student uploadResume(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        Student student = studentRepository.findById(id).orElse(null);

        if (student == null) {
            return null;
        }

        File uploadFolder = new File(UPLOAD_DIR);

        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        String fileName = "student_" + id + "_" + file.getOriginalFilename();

        Path filePath = Paths.get(UPLOAD_DIR + fileName);

        Files.write(filePath, file.getBytes());

        student.setResumeFileName(fileName);

        return studentRepository.save(student);
    }

    /* ================= DOWNLOAD RESUME ================= */

    @GetMapping("/{id}/resume")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long id) {

        Student student = studentRepository.findById(id).orElse(null);

        if (student == null || student.getResumeFileName() == null) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(UPLOAD_DIR + student.getResumeFileName());

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(
                        "Content-Disposition",
                        "attachment; filename=" + student.getResumeFileName()
                )
                .body(resource);
    }

    /* ================= UPLOAD STUDENT PHOTO ================= */

    @PostMapping("/{id}/upload-photo")
    public Student uploadStudentPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        Student student = studentRepository.findById(id).orElse(null);

        if (student == null) {
            return null;
        }

        File uploadFolder = new File(PHOTO_UPLOAD_DIR);

        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        String fileName = "student_" + id + "_" + file.getOriginalFilename();

        Path filePath = Paths.get(PHOTO_UPLOAD_DIR + fileName);

        Files.write(filePath, file.getBytes());

        student.setPhotoFileName(fileName);

        return studentRepository.save(student);
    }

    /* ================= GET STUDENT PHOTO ================= */

    @GetMapping("/{id}/photo")
    public ResponseEntity<Resource> getStudentPhoto(@PathVariable Long id) {

        Student student = studentRepository.findById(id).orElse(null);

        if (student == null || student.getPhotoFileName() == null) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(PHOTO_UPLOAD_DIR + student.getPhotoFileName());

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok().body(resource);
    }
}