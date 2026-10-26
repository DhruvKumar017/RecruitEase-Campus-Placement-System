package com.recruitease.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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
import com.recruitease.entity.PlacementDrive;
import com.recruitease.repository.PlacementDriveRepository;
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
private final PlacementDriveRepository placementDriveRepository;

public InterviewRequestController(
        InterviewRequestRepository interviewRequestRepository,
        ApplicationRepository applicationRepository,
        StudentRepository studentRepository,
        StudentTestResultRepository resultRepository,
        PlacementDriveRepository placementDriveRepository
) {
    this.interviewRequestRepository = interviewRequestRepository;
    this.applicationRepository = applicationRepository;
    this.studentRepository = studentRepository;
    this.resultRepository = resultRepository;
    this.placementDriveRepository = placementDriveRepository;
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

    // Student must qualify the aptitude test.
    StudentTestResult result = resultRepository
            .findTopByApplicationIdOrderBySubmittedAtDesc(applicationId)
            .orElse(null);

    if (result == null ||
            !"Qualified".equalsIgnoreCase(result.getStatus())) {

        return ResponseEntity.badRequest().body(
                "Student must qualify the aptitude test first."
        );
    }

    // Student must have already requested an interview.
    InterviewRequest request = interviewRequestRepository
            .findByApplicationId(applicationId)
            .orElse(null);

    if (request == null) {
        return ResponseEntity.badRequest().body(
                "Student must apply for interview after qualifying the aptitude test."
        );
    }

    // Return existing request without creating duplicates.
    return ResponseEntity.ok(request);
}

    /*
     * Admin sets interview date, time, mode and Google Meet/Classroom link.
     */
    @PutMapping("/api/interview-requests/{id}/schedule")
public ResponseEntity<?> scheduleInterview(
        @PathVariable Long id,
        @RequestParam String interviewDate,
        @RequestParam String interviewTime,
        @RequestParam(defaultValue = "60") Integer interviewDuration,
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

// Check whether the interview is already scheduled.
boolean alreadyScheduled =
        request.getInterviewDate() != null &&
        !request.getInterviewDate().isBlank() &&
        request.getInterviewTime() != null &&
        !request.getInterviewTime().isBlank();

// Block NEW scheduling only after the actual placement drive ends.
if (!alreadyScheduled) {

    Application application = applicationRepository
            .findById(request.getApplicationId())
            .orElse(null);

    if (application == null || application.getDriveId() == null) {
        return ResponseEntity.badRequest()
                .body("Placement drive information is missing.");
    }

    PlacementDrive drive = placementDriveRepository
            .findById(application.getDriveId())
            .orElse(null);

    if (drive == null ||
            drive.getDriveDate() == null ||
            drive.getDriveDate().isBlank() ||
            drive.getDriveEndTime() == null ||
            drive.getDriveEndTime().isBlank()) {

        return ResponseEntity.badRequest()
                .body("Placement drive date or end time is missing.");
    }

    try {

        LocalDate driveDate =
                LocalDate.parse(drive.getDriveDate());

        LocalTime driveEndTime =
                LocalTime.parse(drive.getDriveEndTime());

        LocalDateTime driveEnd =
                LocalDateTime.of(driveDate, driveEndTime);

        if (!LocalDateTime.now().isBefore(driveEnd)) {

            return ResponseEntity.badRequest()
                    .body(
                        "Drive completed. New interviews cannot be scheduled."
                    );
        }

    } catch (DateTimeParseException exception) {

        return ResponseEntity.badRequest()
                .body("Invalid placement drive date or end time.");
    }
}

request.setInterviewDate(interviewDate);
request.setInterviewTime(interviewTime);
        request.setInterviewDuration(interviewDuration);
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
    /*
 * Student can apply only while the interview is UPCOMING.
 */
@PutMapping("/api/interview-requests/{id}/student-apply")
public ResponseEntity<?> studentApplyForInterview(
        @PathVariable Long id
) {

    InterviewRequest request = interviewRequestRepository
            .findById(id)
            .orElse(null);

    if (request == null) {
        return ResponseEntity.notFound().build();
    }

    // Interview must first be scheduled by admin.
    if (request.getInterviewDate() == null ||
            request.getInterviewDate().isBlank() ||
            request.getInterviewTime() == null ||
            request.getInterviewTime().isBlank()) {

        return ResponseEntity.badRequest()
                .body("Interview has not been scheduled yet.");
    }

    // Prevent duplicate application.
    if (request.getStudentAppliedAt() != null) {
        return ResponseEntity.badRequest()
                .body("You have already applied for this interview.");
    }

    try {

        LocalDate interviewDate =
                LocalDate.parse(request.getInterviewDate());

        LocalTime interviewTime =
                LocalTime.parse(request.getInterviewTime());

        LocalDateTime interviewStart =
                LocalDateTime.of(interviewDate, interviewTime);

        LocalDateTime now = LocalDateTime.now();

        // Student can apply only BEFORE interview starts.
        if (!now.isBefore(interviewStart)) {
            return ResponseEntity.badRequest()
                    .body(
                        "Interview has already started. " +
                        "You can no longer apply."
                    );
        }

request.setStudentAppliedAt(now);
request.setInterviewAttempted(false);

InterviewRequest savedRequest =
        interviewRequestRepository.save(request);

        updateApplicationStatus(
                request.getApplicationId(),
                "Interview Applied"
        );

        return ResponseEntity.ok(savedRequest);

    } catch (DateTimeParseException exception) {

        return ResponseEntity.badRequest()
                .body("Invalid interview date or time.");
    }
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
            "Placed"
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

    if ("Placed".equalsIgnoreCase(status)) {
        application.setPlacedAt(LocalDateTime.now());
    }

    applicationRepository.save(application);
}
        }
    }
