package com.recruitease.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitease.dto.StudentNotificationDTO;
import com.recruitease.entity.Application;
import com.recruitease.entity.InterviewRequest;
import com.recruitease.entity.PlacementDrive;
import com.recruitease.entity.Student;
import com.recruitease.entity.StudentNotificationRead;
import com.recruitease.entity.StudentTestResult;
import com.recruitease.repository.ApplicationRepository;
import com.recruitease.repository.InterviewRequestRepository;
import com.recruitease.repository.PlacementDriveRepository;
import com.recruitease.repository.StudentNotificationReadRepository;
import com.recruitease.repository.StudentRepository;
import com.recruitease.repository.StudentTestResultRepository;

@RestController
public class StudentNotificationController {

    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;
    private final StudentTestResultRepository resultRepository;
    private final InterviewRequestRepository interviewRequestRepository;
    private final PlacementDriveRepository placementDriveRepository;
    private final StudentNotificationReadRepository notificationReadRepository;

    public StudentNotificationController(
            StudentRepository studentRepository,
            ApplicationRepository applicationRepository,
            StudentTestResultRepository resultRepository,
            InterviewRequestRepository interviewRequestRepository,
            PlacementDriveRepository placementDriveRepository,
            StudentNotificationReadRepository notificationReadRepository
    ) {
        this.studentRepository = studentRepository;
        this.applicationRepository = applicationRepository;
        this.resultRepository = resultRepository;
        this.interviewRequestRepository = interviewRequestRepository;
        this.placementDriveRepository = placementDriveRepository;
        this.notificationReadRepository = notificationReadRepository;
    }

    /* ================= GET REAL NOTIFICATIONS ================= */

    @GetMapping("/api/student-notifications/{studentId}")
    public ResponseEntity<?> getStudentNotifications(
            @PathVariable Long studentId
    ) {
        Student student = studentRepository.findById(studentId).orElse(null);

        if (student == null || student.isDeleted() || student.isBlocked()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Student account is not active."));
        }

        List<StudentNotificationDTO> notifications = new ArrayList<>();

        List<Application> applications =
                applicationRepository.findByStudentId(studentId);

        Map<Long, Application> applicationMap = new HashMap<>();
        Set<Long> appliedDriveIds = new HashSet<>();

        for (Application application : applications) {
            applicationMap.put(application.getId(), application);

            if (application.getDriveId() != null) {
                appliedDriveIds.add(application.getDriveId());
            }

            addApplicationNotifications(notifications, application);
            addScheduledTestNotification(notifications, application);
        }

        List<StudentTestResult> testResults =
                resultRepository.findByStudentId(studentId);

        for (StudentTestResult result : testResults) {
            Application application =
                    applicationMap.get(result.getApplicationId());

            String companyName = application != null
                    ? safeText(application.getCompanyName(), "Company")
                    : "Company";

            String role = application != null
                    ? safeText(application.getRole(), "placement role")
                    : "placement role";

            boolean qualified =
                    "qualified".equalsIgnoreCase(result.getStatus());

            String title = qualified
                    ? "Aptitude Test Qualified"
                    : "Aptitude Test Result";

            String message;

            if (qualified) {
                message =
                        "Congratulations! You qualified the aptitude test for " +
                        companyName +
                        " - " +
                        role +
                        " with " +
                        safeNumber(result.getPercentage()) +
                        "%.";

            } else {
                message =
                        "Your aptitude test result for " +
                        companyName +
                        " is available. Score: " +
                        safeNumber(result.getPercentage()) +
                        "%.";
            }

            addNotification(
                    notifications,
                    studentId,
                    "TEST_RESULT_" + result.getId(),
                    qualified ? "✅" : "🧠",
                    title,
                    message,
                    "Test",
                    result.getSubmittedAt()
            );
        }

        List<InterviewRequest> interviewRequests =
                interviewRequestRepository.findByStudentId(studentId);

        for (InterviewRequest request : interviewRequests) {
            addInterviewNotifications(notifications, request);
        }

        for (PlacementDrive drive : placementDriveRepository.findAll()) {
            boolean activeDrive = isActiveDrive(drive);

            boolean alreadyApplied =
                    drive.getId() != null &&
                    appliedDriveIds.contains(drive.getId());

            if (
                    activeDrive &&
                    !alreadyApplied &&
                    isStudentEligibleForDrive(student, drive)
            ) {
                String lastDate = safeText(
                        drive.getLastDate(),
                        "the closing date"
                );

                addNotification(
                        notifications,
                        studentId,
                        "ELIGIBLE_DRIVE_" + drive.getId(),
                        "🏢",
                        "Eligible Placement Drive Open",
                        safeText(drive.getCompanyName(), "Company") +
                        " is open for " +
                        safeText(drive.getJobRole(), "a placement role") +
                        ". Apply before " +
                        lastDate +
                        ".",
                        "Placement",
                        toDateTime(drive.getDriveDate(), null)
                );
            }
        }

        notifications.sort(
                Comparator.comparing(
                        StudentNotificationDTO::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
        );

        return ResponseEntity.ok(notifications);
    }

    /* ================= MARK ONE AS READ ================= */

    @PutMapping(
            "/api/student-notifications/{studentId}/read/{notificationKey}"
    )
    public ResponseEntity<?> markNotificationRead(
            @PathVariable Long studentId,
            @PathVariable String notificationKey
    ) {
        Student student = studentRepository.findById(studentId).orElse(null);

        if (student == null) {
            return ResponseEntity.notFound().build();
        }

        StudentNotificationRead notificationRead =
                notificationReadRepository
                        .findByStudentIdAndNotificationKey(
                                studentId,
                                notificationKey
                        )
                        .orElse(new StudentNotificationRead());

        notificationRead.setStudentId(studentId);
        notificationRead.setNotificationKey(notificationKey);
        notificationRead.setReadStatus(true);
        notificationRead.setReadAt(LocalDateTime.now());

        notificationReadRepository.save(notificationRead);

        return ResponseEntity.ok(
                Map.of("message", "Notification marked as read.")
        );
    }

    /* ================= MARK ALL AS READ ================= */

    @PutMapping("/api/student-notifications/{studentId}/read-all")
    public ResponseEntity<?> markAllNotificationsRead(
            @PathVariable Long studentId
    ) {
        Student student = studentRepository.findById(studentId).orElse(null);

        if (student == null) {
            return ResponseEntity.notFound().build();
        }

        List<StudentNotificationDTO> notifications =
                getStudentNotifications(studentId)
                        .getBody() instanceof List<?>
                        ? castNotifications(
                                getStudentNotifications(studentId).getBody()
                        )
                        : new ArrayList<>();

        for (StudentNotificationDTO notification : notifications) {
            StudentNotificationRead notificationRead =
                    notificationReadRepository
                            .findByStudentIdAndNotificationKey(
                                    studentId,
                                    notification.getNotificationKey()
                            )
                            .orElse(new StudentNotificationRead());

            notificationRead.setStudentId(studentId);
            notificationRead.setNotificationKey(
                    notification.getNotificationKey()
            );
            notificationRead.setReadStatus(true);
            notificationRead.setReadAt(LocalDateTime.now());

            notificationReadRepository.save(notificationRead);
        }

        return ResponseEntity.ok(
                Map.of("message", "All notifications marked as read.")
        );
    }

    @SuppressWarnings("unchecked")
    private List<StudentNotificationDTO> castNotifications(Object body) {
        return (List<StudentNotificationDTO>) body;
    }

    /* ================= APPLICATION NOTIFICATIONS ================= */

    private void addApplicationNotifications(
            List<StudentNotificationDTO> notifications,
            Application application
    ) {
        String companyName =
                safeText(application.getCompanyName(), "Company");

        String role =
                safeText(application.getRole(), "placement role");

        addNotification(
                notifications,
                application.getStudentId(),
                "APPLICATION_CREATED_" + application.getId(),
                "📄",
                "Application Submitted",
                "Your application for " +
                role +
                " at " +
                companyName +
                " has been submitted successfully.",
                "Application",
                application.getCreatedAt()
        );

        String status = normalize(application.getStatus());

        if (status.isEmpty() || status.equals("applied")) {
            return;
        }

        String title = "Application Update";
        String icon = "📄";
        String message =
                "Your current application status for " +
                companyName +
                " is " +
                safeText(application.getStatus(), "Updated") +
                ".";

        if (status.equals("shortlisted")) {
            title = "Application Shortlisted";
            icon = "✅";
            message =
                    "You have been shortlisted for " +
                    companyName +
                    ". Check your portal for the next step.";

        } else if (status.equals("test scheduled")) {
            title = "Aptitude Test Scheduled";
            icon = "🧠";

        } else if (
                status.equals("interview") ||
                status.equals("interview scheduled")
        ) {
            title = "Interview Update";
            icon = "🎤";

        } else if (status.equals("interview requested")) {
            title = "Interview Request Submitted";
            icon = "🎤";

        } else if (status.equals("selected")) {
            title = "Congratulations! You Are Selected";
            icon = "🎉";

        } else if (status.equals("placed")) {
            title = "Congratulations! You Are Placed";
            icon = "🏆";

        } else if (
                status.equals("rejected") ||
                status.equals("not selected")
        ) {
            title = "Application Status Update";
            icon = "ℹ️";
        }

        addNotification(
                notifications,
                application.getStudentId(),
                "APPLICATION_STATUS_" +
                application.getId() +
                "_" +
                makeKey(status),
                icon,
                title,
                message,
                "Application",
                application.getCreatedAt()
        );
    }

    private void addScheduledTestNotification(
            List<StudentNotificationDTO> notifications,
            Application application
    ) {
        if (
                application.getTestId() == null ||
                application.getTestDate() == null ||
                application.getTestDate().isBlank()
        ) {
            return;
        }

        String companyName =
                safeText(application.getCompanyName(), "Company");

        String date = safeText(application.getTestDate(), "Not Set");
        String time = safeText(application.getTestStartTime(), "Not Set");

        addNotification(
                notifications,
                application.getStudentId(),
                "TEST_SCHEDULED_" +
                application.getId() +
                "_" +
                application.getTestId(),
                "🧠",
                "Aptitude Test Scheduled",
                companyName +
                " aptitude test is scheduled on " +
                date +
                " at " +
                time +
                ".",
                "Test",
                toDateTime(
                        application.getTestDate(),
                        application.getTestStartTime()
                )
        );
    }

    /* ================= INTERVIEW NOTIFICATIONS ================= */

    private void addInterviewNotifications(
            List<StudentNotificationDTO> notifications,
            InterviewRequest request
    ) {
        String companyName =
                safeText(request.getCompanyName(), "Company");

        addNotification(
                notifications,
                request.getStudentId(),
                "INTERVIEW_REQUEST_" + request.getId(),
                "🎤",
                "Interview Request Submitted",
                "Your interview request for " +
                companyName +
                " has been sent to the admin.",
                "Interview",
                request.getRequestedAt()
        );

        String status = normalize(request.getInterviewStatus());

        if (status.isEmpty() || status.equals("pending")) {
            return;
        }

        String title = "Interview Update";
        String icon = "🎤";
        String message =
                "Interview status for " +
                companyName +
                ": " +
                safeText(request.getInterviewStatus(), "Updated") +
                ".";

        if (status.equals("scheduled")) {
            title = "Interview Scheduled";
            icon = "📅";

            message =
                    companyName +
                    " interview is scheduled on " +
                    safeText(request.getInterviewDate(), "Not Set") +
                    " at " +
                    safeText(request.getInterviewTime(), "Not Set") +
                    ". Mode: " +
                    safeText(request.getInterviewMode(), "Not Set") +
                    ".";

        } else if (status.equals("selected")) {
            title = "Interview Cleared";
            icon = "🎉";

        } else if (status.equals("placed")) {
            title = "Congratulations! You Are Placed";
            icon = "🏆";

        } else if (status.equals("rejected")) {
            title = "Interview Result Update";
            icon = "ℹ️";
        }

        addNotification(
                notifications,
                request.getStudentId(),
                "INTERVIEW_STATUS_" +
                request.getId() +
                "_" +
                makeKey(status),
                icon,
                title,
                message,
                "Interview",
                request.getRequestedAt()
        );
    }

    /* ================= CREATE DTO ================= */

    private void addNotification(
            List<StudentNotificationDTO> notifications,
            Long studentId,
            String notificationKey,
            String icon,
            String title,
            String message,
            String type,
            LocalDateTime createdAt
    ) {
        boolean isRead = notificationReadRepository
                .findByStudentIdAndNotificationKey(
                        studentId,
                        notificationKey
                )
                .map(StudentNotificationRead::isReadStatus)
                .orElse(false);

        StudentNotificationDTO notification =
                new StudentNotificationDTO();

        notification.setNotificationKey(notificationKey);
        notification.setIcon(icon);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setCreatedAt(createdAt);
        notification.setRead(isRead);

        notifications.add(notification);
    }

    /* ================= DRIVE ELIGIBILITY ================= */

    private boolean isActiveDrive(PlacementDrive drive) {
        String status = normalize(drive.getStatus());

        return status.equals("active") || status.equals("open");
    }

    private boolean isStudentEligibleForDrive(
            Student student,
            PlacementDrive drive
    ) {
        if (
                student.getCgpa() == null ||
                drive.getMinCgpa() == null ||
                student.getCgpa() < drive.getMinCgpa()
        ) {
            return false;
        }

        if (!branchMatches(student.getBranch(), drive.getBranch())) {
            return false;
        }

        return skillsMatch(student.getSkills(), drive.getSkills());
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

    /* ================= HELPERS ================= */

    private LocalDateTime toDateTime(
            String dateValue,
            String timeValue
    ) {
        if (dateValue == null || dateValue.isBlank()) {
            return null;
        }

        try {
            LocalDate date = LocalDate.parse(dateValue);

            if (timeValue != null && !timeValue.isBlank()) {
                return LocalDateTime.of(
                        date,
                        LocalTime.parse(timeValue)
                );
            }

            return date.atStartOfDay();

        } catch (Exception error) {
            return null;
        }
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase();
    }

    private String makeKey(String value) {
        return normalize(value)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank()
                ? fallback
                : value.trim();
    }

    private String safeNumber(Double value) {
        if (value == null) {
            return "0";
        }

        return String.format("%.2f", value);
    }
}