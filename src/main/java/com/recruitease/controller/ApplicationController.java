package com.recruitease.controller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.recruitease.dto.ApplicationAdminDTO;
import com.recruitease.entity.Application;
import com.recruitease.entity.AptitudeTest;
import com.recruitease.entity.Company;
import com.recruitease.entity.InterviewRequest;
import com.recruitease.entity.PlacementDrive;
import com.recruitease.entity.Student;
import com.recruitease.entity.StudentTestResult;
import com.recruitease.repository.ApplicationRepository;
import com.recruitease.repository.AptitudeTestRepository;
import com.recruitease.repository.CompanyRepository;
import com.recruitease.repository.InterviewRequestRepository;
import com.recruitease.repository.PlacementDriveRepository;
import com.recruitease.repository.StudentRepository;
import com.recruitease.repository.StudentTestResultRepository;

@RestController
public class ApplicationController {

    private final ApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;
    private final PlacementDriveRepository placementDriveRepository;
    private final StudentRepository studentRepository;
    private final StudentTestResultRepository resultRepository;
    private final InterviewRequestRepository interviewRequestRepository;
    private final AptitudeTestRepository aptitudeTestRepository;

    public ApplicationController(
            ApplicationRepository applicationRepository,
            CompanyRepository companyRepository,
            PlacementDriveRepository placementDriveRepository,
            StudentRepository studentRepository,
            StudentTestResultRepository resultRepository,
            InterviewRequestRepository interviewRequestRepository,
            AptitudeTestRepository aptitudeTestRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.companyRepository = companyRepository;
        this.placementDriveRepository = placementDriveRepository;
        this.studentRepository = studentRepository;
        this.resultRepository = resultRepository;
        this.interviewRequestRepository = interviewRequestRepository;
        this.aptitudeTestRepository = aptitudeTestRepository;
    }

    @PostMapping("/api/applications/apply/{studentId}/{companyId}")
    public ResponseEntity<?> applyCompany(
            @PathVariable Long studentId,
            @PathVariable Long companyId
    ) {
        Optional<Application> existing =
                applicationRepository.findByStudentIdAndCompanyId(studentId, companyId);

        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get());
        }

        Student student = studentRepository.findById(studentId).orElse(null);

        if (student == null || student.isDeleted() || student.isBlocked()) {
            return ResponseEntity.badRequest()
                    .body("Student account is not active.");
        }

        Company company = companyRepository.findById(companyId).orElse(null);

        if (company == null) {
            return ResponseEntity.notFound().build();
        }

        Application application = new Application();

        application.setStudentId(studentId);
        application.setCompanyId(companyId);
        application.setCompanyName(company.getCompanyName());
        application.setRole("Software Engineer");
        application.setStatus("Applied");

        return ResponseEntity.ok(applicationRepository.save(application));
    }

    @PostMapping("/api/applications/apply-drive/{studentId}/{driveId}")
    public ResponseEntity<?> applyForDrive(
            @PathVariable Long studentId,
            @PathVariable Long driveId
    ) {
        Optional<Application> existing =
                applicationRepository.findByStudentIdAndDriveId(studentId, driveId);

        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get());
        }

        Student student = studentRepository.findById(studentId).orElse(null);

        if (student == null || student.isDeleted() || student.isBlocked()) {
            return ResponseEntity.badRequest()
                    .body("Student account is not active.");
        }

        PlacementDrive drive =
                placementDriveRepository.findById(driveId).orElse(null);

        if (drive == null) {
            return ResponseEntity.notFound().build();
        }

        if (!isDriveOpen(drive)) {
            return ResponseEntity.badRequest()
                    .body("This placement drive is closed.");
        }

        if (!isStudentEligibleForDrive(student, drive)) {
            return ResponseEntity.badRequest()
                    .body("You are not eligible for this placement drive.");
        }

        Company company = companyRepository
                .findByCompanyNameIgnoreCase(drive.getCompanyName())
                .orElse(null);

        if (company == null) {
            return ResponseEntity.badRequest()
                    .body("Drive company is not available in Companies data.");
        }

        Application application = new Application();

        application.setStudentId(studentId);
        application.setCompanyId(company.getId());
        application.setDriveId(driveId);
        application.setCompanyName(drive.getCompanyName());
        application.setRole(drive.getJobRole());
        application.setStatus("Applied");

        return ResponseEntity.ok(applicationRepository.save(application));
    }

    @GetMapping("/api/applications/student/{studentId}")
    public List<Application> getStudentApplications(@PathVariable Long studentId) {
        return applicationRepository.findByStudentId(studentId);
    }

    @GetMapping("/api/applications/all")
    public List<ApplicationAdminDTO> getAllApplications() {
        return applicationRepository.findAll()
                .stream()
                .map(this::convertToAdminDTO)
                .toList();
    }

    @GetMapping("/api/applications/student/{studentId}/company/{companyId}")
    public boolean alreadyApplied(
            @PathVariable Long studentId,
            @PathVariable Long companyId
    ) {
        return applicationRepository
                .findByStudentIdAndCompanyId(studentId, companyId)
                .isPresent();
    }

    @GetMapping("/api/applications/student/{studentId}/drive/{driveId}")
    public boolean alreadyAppliedForDrive(
            @PathVariable Long studentId,
            @PathVariable Long driveId
    ) {
        return applicationRepository
                .findByStudentIdAndDriveId(studentId, driveId)
                .isPresent();
    }

    @GetMapping("/api/applications/{applicationId}/interview-eligibility")
    public ResponseEntity<?> getInterviewEligibility(
            @PathVariable Long applicationId
    ) {
        Application application =
                applicationRepository.findById(applicationId).orElse(null);

        if (application == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new LinkedHashMap<>();

        StudentTestResult result = resultRepository
                .findTopByApplicationIdOrderBySubmittedAtDesc(applicationId)
                .orElse(null);

        Company company = getCompanyForApplication(application);
        double cutoff = getCompanyCutoff(company);

        boolean qualified = result != null &&
                "Qualified".equalsIgnoreCase(result.getStatus());

        boolean interviewRequested =
                interviewRequestRepository.existsByApplicationId(applicationId);

        response.put("applicationId", applicationId);
        response.put("testTaken", result != null);
        response.put("qualified", qualified);
        response.put("interviewRequested", interviewRequested);
        response.put("canApplyForInterview", qualified && !interviewRequested);
        response.put("companyCutoff", cutoff);

        if (result != null) {
            response.put("obtainedMarks", result.getObtainedMarks());
            response.put("totalMarks", result.getTotalMarks());
            response.put("percentage", result.getPercentage());
            response.put("testResultStatus", result.getStatus());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/applications/{applicationId}/request-interview/{studentId}")
    public ResponseEntity<?> requestInterview(
            @PathVariable Long applicationId,
            @PathVariable Long studentId
    ) {
        Application application =
                applicationRepository.findById(applicationId).orElse(null);

        if (application == null) {
            return ResponseEntity.notFound().build();
        }

        if (!application.getStudentId().equals(studentId)) {
            return ResponseEntity.badRequest()
                    .body("This application does not belong to this student.");
        }

        if (interviewRequestRepository.existsByApplicationId(applicationId)) {
            return ResponseEntity.badRequest()
                    .body("Interview request is already submitted.");
        }

        StudentTestResult result = resultRepository
                .findTopByApplicationIdOrderBySubmittedAtDesc(applicationId)
                .orElse(null);

        if (result == null) {
            return ResponseEntity.badRequest()
                    .body("Complete the aptitude test first.");
        }

        if (!"Qualified".equalsIgnoreCase(result.getStatus())) {
            return ResponseEntity.badRequest()
                    .body("You did not meet the company aptitude cutoff.");
        }

        Student student = studentRepository.findById(studentId).orElse(null);
        Company company = getCompanyForApplication(application);

        if (student == null || company == null) {
            return ResponseEntity.badRequest()
                    .body("Student or company data not found.");
        }

        InterviewRequest request = new InterviewRequest();

        request.setStudentId(studentId);
        request.setStudentName(student.getName());
        request.setStudentEmail(student.getEmail());
        request.setCompanyId(company.getId());
        request.setCompanyName(company.getCompanyName());
        request.setApplicationId(applicationId);
        request.setTestId(application.getTestId());
        request.setAptitudePercentage(result.getPercentage());
        request.setInterviewStatus("PENDING");

        InterviewRequest savedRequest =
                interviewRequestRepository.save(request);

        application.setStatus("Interview Requested");
        applicationRepository.save(application);

        return ResponseEntity.ok(savedRequest);
    }

    @PutMapping("/api/applications/{id}/status/{status}")
public ResponseEntity<?> updateApplicationStatus(
        @PathVariable Long id,
        @PathVariable String status
) {
    Application application =
            applicationRepository.findById(id).orElse(null);

    if (application == null) {
        return ResponseEntity.notFound().build();
    }

    String normalizedStatus = status.trim().toLowerCase();

    boolean requiresInterviewRequest =
            normalizedStatus.equals("interview") ||
            normalizedStatus.equals("selected") ||
            normalizedStatus.equals("placed");

    if (requiresInterviewRequest &&
            !canAdminUseInterviewActions(application)) {

        return ResponseEntity.badRequest().body(
                "Student must qualify the aptitude test and request an interview first."
        );
    }

    application.setStatus(status);

    Application savedApplication =
            applicationRepository.save(application);

    String interviewStatus = null;

    if (normalizedStatus.equals("placed")) {
        interviewStatus = "PLACED";

    } else if (normalizedStatus.equals("selected")) {
        interviewStatus = "SELECTED";

    } else if (normalizedStatus.equals("rejected")) {
        interviewStatus = "REJECTED";
    }

    if (interviewStatus != null) {
        final String finalInterviewStatus = interviewStatus;

        interviewRequestRepository
                .findByApplicationId(id)
                .ifPresent(request -> {
                    request.setInterviewStatus(finalInterviewStatus);
                    interviewRequestRepository.save(request);
                });
    }

    return ResponseEntity.ok(savedApplication);
}

    /*
     * New schedule:
     * System generates a fresh Test ID automatically.
     *
     * Reschedule:
     * Same Test ID stays, so existing questions remain connected.
     */
    @PutMapping("/api/applications/{id}/schedule-test")
    public ResponseEntity<?> scheduleTest(
            @PathVariable Long id,
            @RequestParam String testDate,
            @RequestParam String testStartTime,
            @RequestParam String testEndTime
    ) {
        if (testDate == null || testDate.isBlank() ||
                testStartTime == null || testStartTime.isBlank() ||
                testEndTime == null || testEndTime.isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Test date, start time and end time are required.");
        }

        Application application =
                applicationRepository.findById(id).orElse(null);

        if (application == null) {
            return ResponseEntity.notFound().build();
        }

        boolean testAlreadyCompleted = resultRepository
                .findTopByApplicationIdOrderBySubmittedAtDesc(id)
                .isPresent();

        if (testAlreadyCompleted) {
            return ResponseEntity.badRequest()
                    .body("Student has already completed this aptitude test.");
        }

        AptitudeTest aptitudeTest = null;
        boolean newTestGenerated = false;

        if (application.getTestId() != null) {
            aptitudeTest = aptitudeTestRepository
                    .findById(application.getTestId())
                    .orElse(null);
        }

        if (aptitudeTest == null) {
            aptitudeTest = createStudentSpecificTest(
                    application,
                    testDate,
                    testStartTime,
                    testEndTime
            );

            newTestGenerated = true;

        } else {
            aptitudeTest.setStatus("Scheduled");
            aptitudeTest.setScheduledDay(getDayName(testDate));
            aptitudeTest.setDuration(
                    calculateDuration(testStartTime, testEndTime)
            );

            aptitudeTestRepository.save(aptitudeTest);
        }

        application.setTestId(aptitudeTest.getId());
        application.setTestDate(testDate);
        application.setTestStartTime(testStartTime);
        application.setTestEndTime(testEndTime);
        application.setTestPermission("Allowed");
        application.setStatus("Test Scheduled");

        Application savedApplication =
                applicationRepository.save(application);

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("message", "Student test scheduled successfully.");
        response.put("generatedTestId", aptitudeTest.getId());
        response.put("testName", aptitudeTest.getTestName());
        response.put("generatedNewTest", newTestGenerated);
        response.put("application", savedApplication);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/applications/student/{studentId}/scheduled-tests")
    public List<Application> getScheduledTests(@PathVariable Long studentId) {
        return applicationRepository.findByStudentId(studentId)
                .stream()
                .filter(app -> app.getTestId() != null)
                .filter(app -> app.getTestDate() != null)
                .filter(app -> app.getTestStartTime() != null)
                .filter(app -> app.getTestEndTime() != null)
                .toList();
    }

    private AptitudeTest createStudentSpecificTest(
            Application application,
            String testDate,
            String startTime,
            String endTime
    ) {
        Student student = studentRepository
                .findById(application.getStudentId())
                .orElse(null);

        String studentName = student != null && student.getName() != null
                ? student.getName()
                : "Student " + application.getStudentId();

        String companyName = application.getCompanyName() != null
                ? application.getCompanyName()
                : "Company";

        AptitudeTest test = new AptitudeTest();

        test.setTestName(companyName + " Test - " + studentName);
        test.setTestType("Online");
        test.setDuration(calculateDuration(startTime, endTime));
        test.setScheduledDay(getDayName(testDate));
        test.setStatus("Scheduled");
        test.setScore(0);

        return aptitudeTestRepository.save(test);
    }

    private int calculateDuration(String startTime, String endTime) {
        try {
            LocalTime start = LocalTime.parse(startTime);
            LocalTime end = LocalTime.parse(endTime);

            long minutes = Duration.between(start, end).toMinutes();

            return minutes > 0 ? (int) minutes : 60;

        } catch (Exception error) {
            return 60;
        }
    }

    private String getDayName(String testDate) {
        try {
            return LocalDate.parse(testDate)
                    .getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        } catch (Exception error) {
            return "Not Set";
        }
    }

    private ApplicationAdminDTO convertToAdminDTO(Application application) {
        ApplicationAdminDTO dto = new ApplicationAdminDTO();

        dto.setId(application.getId());
        dto.setStudentId(application.getStudentId());
        dto.setCompanyId(application.getCompanyId());
        dto.setDriveId(application.getDriveId());

        dto.setCompanyName(application.getCompanyName());
        dto.setRole(application.getRole());
        dto.setStatus(application.getStatus());

        dto.setTestId(application.getTestId());
        dto.setTestDate(application.getTestDate());
        dto.setTestStartTime(application.getTestStartTime());
        dto.setTestEndTime(application.getTestEndTime());
        dto.setTestPermission(application.getTestPermission());

        Student student = null;

if (application.getStudentId() != null) {
    student = studentRepository
            .findById(application.getStudentId())
            .orElse(null);
}

if (student != null) {
    dto.setStudentName(student.getName());
} else {
    dto.setStudentName("Unknown Student");
}

        Company company = getCompanyForApplication(application);
        dto.setCompanyCutoff(getCompanyCutoff(company));

        StudentTestResult result = resultRepository
                .findTopByApplicationIdOrderBySubmittedAtDesc(application.getId())
                .orElse(null);

        if (result != null) {
            dto.setObtainedMarks(result.getObtainedMarks());
            dto.setTotalMarks(result.getTotalMarks());
            dto.setPercentage(result.getPercentage());
            dto.setTestResultStatus(result.getStatus());
        }

        InterviewRequest interviewRequest = interviewRequestRepository
                .findByApplicationId(application.getId())
                .orElse(null);

        if (interviewRequest != null) {
            dto.setInterviewRequested(true);
            dto.setInterviewStatus(interviewRequest.getInterviewStatus());
        } else {
            dto.setInterviewRequested(false);
        }

        return dto;
    }

    private Company getCompanyForApplication(Application application) {
        if (application.getCompanyId() != null) {
            Company company = companyRepository
                    .findById(application.getCompanyId())
                    .orElse(null);

            if (company != null) {
                return company;
            }
        }

        if (application.getCompanyName() != null &&
                !application.getCompanyName().trim().isEmpty()) {

            return companyRepository
                    .findByCompanyNameIgnoreCase(
                            application.getCompanyName().trim()
                    )
                    .orElse(null);
        }

        return null;
    }

    private double getCompanyCutoff(Company company) {
        if (company != null && company.getAptitudeCutoff() != null) {
            return company.getAptitudeCutoff();
        }

        return 50.0;
    }

    private boolean canAdminUseInterviewActions(Application application) {
        StudentTestResult result = resultRepository
                .findTopByApplicationIdOrderBySubmittedAtDesc(application.getId())
                .orElse(null);

        boolean qualified = result != null &&
                "Qualified".equalsIgnoreCase(result.getStatus());

        boolean interviewRequested =
                interviewRequestRepository.existsByApplicationId(application.getId());

        return qualified && interviewRequested;
    }

    private boolean isDriveOpen(PlacementDrive drive) {
        String status = drive.getStatus() == null
                ? ""
                : drive.getStatus().trim().toLowerCase();

        if (!status.equals("upcoming") && !status.equals("active")) {
            return false;
        }

        if (drive.getLastDate() == null || drive.getLastDate().trim().isEmpty()) {
            return true;
        }

        try {
            LocalDate lastDate = LocalDate.parse(drive.getLastDate());
            return !lastDate.isBefore(LocalDate.now());

        } catch (Exception error) {
            return true;
        }
    }

    private boolean isStudentEligibleForDrive(Student student, PlacementDrive drive) {
        if (student.getCgpa() == null ||
                drive.getMinCgpa() == null ||
                student.getCgpa() < drive.getMinCgpa()) {
            return false;
        }

        if (!branchMatches(student.getBranch(), drive.getBranch())) {
            return false;
        }

        return skillsMatch(student.getSkills(), drive.getSkills());
    }

    private boolean branchMatches(String studentBranch, String allowedBranches) {
        String allowedText = allowedBranches == null
                ? ""
                : allowedBranches.trim().toLowerCase();

        if (allowedText.isEmpty() || allowedText.equals("all")) {
            return true;
        }

        String studentText = studentBranch == null
                ? ""
                : studentBranch.trim().toLowerCase();

        for (String branch : allowedText.split("[,/|]")) {
            String cleanBranch = branch.trim();

            if (studentText.equals(cleanBranch) ||
                    studentText.contains(cleanBranch) ||
                    cleanBranch.contains(studentText)) {
                return true;
            }
        }

        return false;
    }

    private boolean skillsMatch(String studentSkills, String requiredSkills) {
        String requiredText = requiredSkills == null
                ? ""
                : requiredSkills.trim().toLowerCase();

        if (requiredText.isEmpty()) {
            return true;
        }

        String studentText = studentSkills == null
                ? ""
                : studentSkills.trim().toLowerCase();

        for (String skill : requiredText.split("[,/|]")) {
            if (studentText.contains(skill.trim())) {
                return true;
            }
        }

        return false;
    }
}