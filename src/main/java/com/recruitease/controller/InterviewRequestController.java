package com.recruitease.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.recruitease.entity.Application;
import com.recruitease.entity.InterviewRequest;
import com.recruitease.entity.Student;
import com.recruitease.entity.StudentTestResult;
import com.recruitease.repository.ApplicationRepository;
import com.recruitease.repository.InterviewRequestRepository;
import com.recruitease.repository.StudentRepository;
import com.recruitease.repository.StudentTestResultRepository;

@RestController
public class InterviewRequestController {

    private final InterviewRequestRepository interviewRequestRepository;
    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final StudentTestResultRepository resultRepository;

    public InterviewRequestController(
            InterviewRequestRepository interviewRequestRepository,
            ApplicationRepository applicationRepository,
            StudentRepository studentRepository,
            StudentTestResultRepository resultRepository
    ) {
        this.interviewRequestRepository = interviewRequestRepository;
        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
        this.resultRepository = resultRepository;
    }

    @GetMapping("/api/interview-requests")
    public List<InterviewRequest> getAllInterviewRequests() {
        return interviewRequestRepository.findAll();
    }

    @GetMapping("/api/interview-requests/student/{studentId}")
    public List<InterviewRequest> getStudentInterviewRequests(
            @PathVariable Long studentId
    ) {
        return interviewRequestRepository.findByStudentId(studentId);
    }

    @GetMapping("/api/interview-requests/{id}")
    public ResponseEntity<?> getInterviewRequestById(@PathVariable Long id) {
        InterviewRequest request = interviewRequestRepository
                .findById(id)
                .orElse(null);

        if (request == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(request);
    }

    /*
     * Admin directly sends a qualified student to Interview Manager.
     */
    @PostMapping("/api/interview-requests/admin-send/{applicationId}")
    public ResponseEntity<?> adminSendToInterview(
            @PathVariable Long applicationId
    ) {
        Application application = applicationRepository
                .findById(applicationId)
                .orElse(null);

        if (application == null) {
            return ResponseEntity.notFound().build();
        }

        Optional<InterviewRequest> existingRequest =
                interviewRequestRepository.findByApplicationId(applicationId);

        if (existingRequest.isPresent()) {
            return ResponseEntity.ok(existingRequest.get());
        }

        StudentTestResult result = resultRepository
                .findTopByApplicationIdOrderBySubmittedAtDesc(applicationId)
                .orElse(null);

        if (result == null ||
                !"Qualified".equalsIgnoreCase(result.getStatus())) {

            return ResponseEntity.badRequest().body(
                    "Only qualified students can be sent for interview."
            );
        }

        Student student = studentRepository
                .findById(application.getStudentId())
                .orElse(null);

        if (student == null) {
            return ResponseEntity.badRequest()
                    .body("Student data not found.");
        }

        InterviewRequest request = new InterviewRequest();

        request.setStudentId(student.getId());
        request.setStudentName(student.getName());
        request.setStudentEmail(student.getEmail());

        request.setCompanyId(application.getCompanyId());
        request.setCompanyName(application.getCompanyName());

        request.setApplicationId(application.getId());
        request.setTestId(application.getTestId());
        request.setAptitudePercentage(result.getPercentage());

        request.setInterviewStatus("PENDING");

        InterviewRequest savedRequest =
                interviewRequestRepository.save(request);

        application.setStatus("Interview Requested");
        applicationRepository.save(application);

        return ResponseEntity.ok(savedRequest);
    }

    /*
     * Admin sets interview date, time, mode and Google Meet/Classroom link.
     */
    @PutMapping("/api/interview-requests/{id}/schedule")
    public ResponseEntity<?> scheduleInterview(
            @PathVariable Long id,
            @RequestParam String interviewDate,
            @RequestParam String interviewTime,
            @RequestParam(defaultValue = "Google Meet") String interviewMode,
            @RequestParam(required = false) String interviewLink,
            @RequestParam(required = false) String adminRemark
    ) {
        if (interviewDate == null || interviewDate.isBlank() ||
                interviewTime == null || interviewTime.isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Interview date and time are required.");
        }

        InterviewRequest request = interviewRequestRepository
                .findById(id)
                .orElse(null);

        if (request == null) {
            return ResponseEntity.notFound().build();
        }

        request.setInterviewDate(interviewDate);
        request.setInterviewTime(interviewTime);
        request.setInterviewMode(interviewMode);
        request.setInterviewLink(interviewLink);
        request.setAdminRemark(adminRemark);
        request.setInterviewStatus("SCHEDULED");

        InterviewRequest savedRequest =
                interviewRequestRepository.save(request);

        updateApplicationStatus(
                request.getApplicationId(),
                "Interview Scheduled"
        );

        return ResponseEntity.ok(savedRequest);
    }

    @PutMapping("/api/interview-requests/{id}/status/{status}")
    public ResponseEntity<?> updateInterviewStatus(
            @PathVariable Long id,
            @PathVariable String status
    ) {
        InterviewRequest request = interviewRequestRepository
                .findById(id)
                .orElse(null);

        if (request == null) {
            return ResponseEntity.notFound().build();
        }

        request.setInterviewStatus(status);

        InterviewRequest savedRequest =
                interviewRequestRepository.save(request);

        String normalizedStatus = status.trim().toLowerCase();

        if (normalizedStatus.equals("selected")) {
            updateApplicationStatus(
                    request.getApplicationId(),
                    "Selected"
            );
        } else if (normalizedStatus.equals("rejected")) {
            updateApplicationStatus(
                    request.getApplicationId(),
                    "Rejected"
            );
        } else if (normalizedStatus.equals("completed")) {
            updateApplicationStatus(
                    request.getApplicationId(),
                    "Interview Completed"
            );
        }

        return ResponseEntity.ok(savedRequest);
    }

    private void updateApplicationStatus(
            Long applicationId,
            String status
    ) {
        if (applicationId == null) {
            return;
        }

        Application application = applicationRepository
                .findById(applicationId)
                .orElse(null);

        if (application != null) {
            application.setStatus(status);
            applicationRepository.save(application);
        }
    }
}