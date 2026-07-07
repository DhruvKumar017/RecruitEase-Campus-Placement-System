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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.recruitease.dto.AdminNotificationDTO;
import com.recruitease.entity.AdminNotificationRead;
import com.recruitease.entity.AdminRequest;
import com.recruitease.entity.Application;
import com.recruitease.entity.InterviewRequest;
import com.recruitease.entity.Student;
import com.recruitease.entity.StudentIssue;
import com.recruitease.repository.AdminNotificationReadRepository;
import com.recruitease.repository.AdminRequestRepository;
import com.recruitease.repository.ApplicationRepository;
import com.recruitease.repository.InterviewRequestRepository;
import com.recruitease.repository.StudentIssueRepository;
import com.recruitease.repository.StudentRepository;

@RestController
@RequestMapping("/api/admin-notifications")
public class AdminNotificationController {

    private final AdminRequestRepository adminRequestRepository;
    private final StudentIssueRepository studentIssueRepository;
    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRequestRepository interviewRequestRepository;
    private final AdminNotificationReadRepository notificationReadRepository;

    public AdminNotificationController(
            AdminRequestRepository adminRequestRepository,
            StudentIssueRepository studentIssueRepository,
            StudentRepository studentRepository,
            ApplicationRepository applicationRepository,
            InterviewRequestRepository interviewRequestRepository,
            AdminNotificationReadRepository notificationReadRepository
    ) {
        this.adminRequestRepository = adminRequestRepository;
        this.studentIssueRepository = studentIssueRepository;
        this.studentRepository = studentRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRequestRepository = interviewRequestRepository;
        this.notificationReadRepository = notificationReadRepository;
    }

    /* ================= GET ALL LIVE NOTIFICATIONS ================= */

    @GetMapping
    public ResponseEntity<?> getAdminNotifications(
            @RequestParam String adminUsername
    ) {
        String cleanAdminUsername = normalizeAdminUsername(adminUsername);

        if (cleanAdminUsername.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Admin username is required.");
        }

        List<AdminNotificationDTO> notifications =
                buildLiveNotifications(cleanAdminUsername);

        return ResponseEntity.ok(notifications);
    }

    /* ================= MARK ONE AS READ ================= */

    @PostMapping("/mark-read")
    public ResponseEntity<?> markNotificationRead(
            @RequestParam String adminUsername,
            @RequestParam String notificationKey
    ) {
        String cleanAdminUsername = normalizeAdminUsername(adminUsername);
        String cleanNotificationKey = notificationKey == null
                ? ""
                : notificationKey.trim();

        if (cleanAdminUsername.isEmpty() ||
                cleanNotificationKey.isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Admin username and notification key are required.");
        }

        boolean alreadyRead =
                notificationReadRepository
                        .existsByAdminUsernameAndNotificationKey(
                                cleanAdminUsername,
                                cleanNotificationKey
                        );

        if (!alreadyRead) {
            AdminNotificationRead read = new AdminNotificationRead();

            read.setAdminUsername(cleanAdminUsername);
            read.setNotificationKey(cleanNotificationKey);

            notificationReadRepository.save(read);
        }

        return ResponseEntity.ok(
                Map.of(
                        "message", "Notification marked as read.",
                        "notificationKey", cleanNotificationKey
                )
        );
    }

    /* ================= MARK ALL AS READ ================= */

    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllNotificationsRead(
            @RequestParam String adminUsername
    ) {
        String cleanAdminUsername = normalizeAdminUsername(adminUsername);

        if (cleanAdminUsername.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Admin username is required.");
        }

        List<AdminNotificationDTO> notifications =
                buildLiveNotifications(cleanAdminUsername);

        int markedCount = 0;

        for (AdminNotificationDTO notification : notifications) {
            if (!notification.isRead()) {
                AdminNotificationRead read = new AdminNotificationRead();

                read.setAdminUsername(cleanAdminUsername);
                read.setNotificationKey(
                        notification.getNotificationKey()
                );

                notificationReadRepository.save(read);
                markedCount++;
            }
        }

        return ResponseEntity.ok(
                Map.of(
                        "message", "All notifications marked as read.",
                        "markedCount", markedCount
                )
        );
    }

    /* ================= BUILD ALL REAL NOTIFICATIONS ================= */

    private List<AdminNotificationDTO> buildLiveNotifications(
            String adminUsername
    ) {
        List<AdminNotificationDTO> notifications = new ArrayList<>();

        List<AdminNotificationRead> readEntries =
                notificationReadRepository.findByAdminUsername(
                        adminUsername
                );

        Set<String> readKeys = new HashSet<>();

        for (AdminNotificationRead readEntry : readEntries) {
            readKeys.add(readEntry.getNotificationKey());
        }

        Map<Long, Student> studentsById = new HashMap<>();

        for (Student student : studentRepository.findAll()) {
            studentsById.put(student.getId(), student);
        }

        addAdminRequestNotifications(notifications);
        addStudentIssueNotifications(notifications);
        addStudentRegistrationNotifications(notifications);
        addApplicationNotifications(notifications, studentsById);
        addInterviewRequestNotifications(notifications);
        addUpcomingTestNotifications(notifications, studentsById);
        addUpcomingInterviewNotifications(notifications);

        for (AdminNotificationDTO notification : notifications) {
            notification.setRead(
                    readKeys.contains(notification.getNotificationKey())
            );
        }

       notifications.sort(
        Comparator.comparing(
                (AdminNotificationDTO notification) -> {
                    LocalDateTime createdAt =
                            notification.getCreatedAt();

                    return createdAt == null
                            ? LocalDateTime.MIN
                            : createdAt;
                }
        ).reversed()
);
        return notifications.stream()
                .limit(100)
                .toList();
    }

    /* ================= ADMIN ACCESS REQUESTS ================= */

    private void addAdminRequestNotifications(
            List<AdminNotificationDTO> notifications
    ) {
        List<AdminRequest> requests =
                adminRequestRepository.findByStatusIgnoreCase("Pending");

        for (AdminRequest request : requests) {
            AdminNotificationDTO notification =
                    new AdminNotificationDTO();

            notification.setNotificationKey(
                    "ADMIN_REQUEST_" + request.getId()
            );

            notification.setType("ADMIN_REQUEST");
            notification.setIcon("👨‍💼");
            notification.setTitle("New Admin Access Request");

            notification.setMessage(
                    safeText(request.getFullName(), "Admin") +
                    " requested admin access."
            );

            notification.setCreatedAt(request.getRequestedAt());
            notification.setTargetPage("requests-issues.html");

            notifications.add(notification);
        }
    }

    /* ================= STUDENT ISSUES ================= */

    private void addStudentIssueNotifications(
            List<AdminNotificationDTO> notifications
    ) {
        List<StudentIssue> issues =
                studentIssueRepository.findByStatusIgnoreCase("Pending");

        for (StudentIssue issue : issues) {
            AdminNotificationDTO notification =
                    new AdminNotificationDTO();

            notification.setNotificationKey(
                    "STUDENT_ISSUE_" + issue.getId()
            );

            notification.setType("STUDENT_ISSUE");
            notification.setIcon("🛟");
            notification.setTitle("New Student Issue");

            notification.setMessage(
                    safeText(issue.getStudentName(), "Student") +
                    " raised: " +
                    safeText(issue.getIssueType(), "General Issue")
            );

            notification.setCreatedAt(issue.getSubmittedAt());
            notification.setTargetPage("requests-issues.html");

            notifications.add(notification);
        }
    }

    /* ================= NEW STUDENT REGISTRATIONS ================= */

    private void addStudentRegistrationNotifications(
            List<AdminNotificationDTO> notifications
    ) {
        List<Student> students =
                studentRepository.findByDeleted(false);

        for (Student student : students) {
            if (student.getCreatedAt() == null) {
                continue;
            }

            AdminNotificationDTO notification =
                    new AdminNotificationDTO();

            notification.setNotificationKey(
                    "STUDENT_REGISTRATION_" + student.getId()
            );

            notification.setType("STUDENT_REGISTRATION");
            notification.setIcon("🎓");
            notification.setTitle("New Student Registration");

            notification.setMessage(
                    safeText(student.getName(), "Student") +
                    " registered for placement portal."
            );

            notification.setCreatedAt(student.getCreatedAt());
            notification.setTargetPage("students.html");

            notifications.add(notification);
        }
    }

    /* ================= NEW JOB APPLICATIONS ================= */

    private void addApplicationNotifications(
            List<AdminNotificationDTO> notifications,
            Map<Long, Student> studentsById
    ) {
        List<Application> applications =
                applicationRepository.findAll();

        for (Application application : applications) {
            if (application.getCreatedAt() == null) {
                continue;
            }

            Student student =
                    studentsById.get(application.getStudentId());

            String studentName = student == null
                    ? "Student"
                    : safeText(student.getName(), "Student");

            AdminNotificationDTO notification =
                    new AdminNotificationDTO();

            notification.setNotificationKey(
                    "JOB_APPLICATION_" + application.getId()
            );

            notification.setType("JOB_APPLICATION");
            notification.setIcon("📄");
            notification.setTitle("New Job Application");

            notification.setMessage(
                    studentName +
                    " applied for " +
                    safeText(application.getCompanyName(), "a company") +
                    "."
            );

            notification.setCreatedAt(application.getCreatedAt());
            notification.setTargetPage("applications-admin.html");

            notifications.add(notification);
        }
    }

    /* ================= PENDING INTERVIEW REQUESTS ================= */

    private void addInterviewRequestNotifications(
            List<AdminNotificationDTO> notifications
    ) {
        List<InterviewRequest> interviewRequests =
                interviewRequestRepository.findAll();

        for (InterviewRequest request : interviewRequests) {
            String status = safeText(
                    request.getInterviewStatus(),
                    ""
            );

            if (!status.equalsIgnoreCase("PENDING")) {
                continue;
            }

            AdminNotificationDTO notification =
                    new AdminNotificationDTO();

            notification.setNotificationKey(
                    "INTERVIEW_REQUEST_" + request.getId()
            );

            notification.setType("INTERVIEW_REQUEST");
            notification.setIcon("🎤");
            notification.setTitle("New Interview Request");

            notification.setMessage(
                    safeText(request.getStudentName(), "Student") +
                    " requested interview for " +
                    safeText(request.getCompanyName(), "company") +
                    "."
            );

            notification.setCreatedAt(request.getRequestedAt());
            notification.setTargetPage("admin-interviews.html");

            notifications.add(notification);
        }
    }

    /* ================= UPCOMING APTITUDE TESTS ================= */

    private void addUpcomingTestNotifications(
            List<AdminNotificationDTO> notifications,
            Map<Long, Student> studentsById
    ) {
        LocalDate today = LocalDate.now();

        List<Application> applications =
                applicationRepository.findAll();

        for (Application application : applications) {
            if (application.getTestId() == null ||
                    application.getTestDate() == null ||
                    application.getTestDate().isBlank()) {

                continue;
            }

            LocalDate testDate = parseDate(application.getTestDate());

            if (testDate == null || testDate.isBefore(today)) {
                continue;
            }

            Student student =
                    studentsById.get(application.getStudentId());

            String studentName = student == null
                    ? "Student"
                    : safeText(student.getName(), "Student");

            AdminNotificationDTO notification =
                    new AdminNotificationDTO();

            notification.setNotificationKey(
                    "UPCOMING_TEST_" + application.getId()
            );

            notification.setType("UPCOMING_TEST");
            notification.setIcon("🧠");
            notification.setTitle("Upcoming Aptitude Test");

            notification.setMessage(
                    studentName +
                    " has test for " +
                    safeText(application.getCompanyName(), "company") +
                    " on " +
                    formatDate(application.getTestDate()) +
                    "."
            );

            notification.setCreatedAt(
                    createDateTime(
                            application.getTestDate(),
                            application.getTestStartTime()
                    )
            );

            notification.setTargetPage("admin-aptitude-tests.html");

            notifications.add(notification);
        }
    }

    /* ================= UPCOMING INTERVIEWS ================= */

    private void addUpcomingInterviewNotifications(
            List<AdminNotificationDTO> notifications
    ) {
        LocalDate today = LocalDate.now();

        List<InterviewRequest> interviewRequests =
                interviewRequestRepository.findAll();

        for (InterviewRequest request : interviewRequests) {
            String status = safeText(
                    request.getInterviewStatus(),
                    ""
            );

            if (!status.equalsIgnoreCase("SCHEDULED")) {
                continue;
            }

            if (request.getInterviewDate() == null ||
                    request.getInterviewDate().isBlank()) {

                continue;
            }

            LocalDate interviewDate =
                    parseDate(request.getInterviewDate());

            if (interviewDate == null ||
                    interviewDate.isBefore(today)) {

                continue;
            }

            AdminNotificationDTO notification =
                    new AdminNotificationDTO();

            notification.setNotificationKey(
                    "UPCOMING_INTERVIEW_" + request.getId()
            );

            notification.setType("UPCOMING_INTERVIEW");
            notification.setIcon("📅");
            notification.setTitle("Upcoming Interview");

            notification.setMessage(
                    safeText(request.getStudentName(), "Student") +
                    " interview with " +
                    safeText(request.getCompanyName(), "company") +
                    " is on " +
                    formatDate(request.getInterviewDate()) +
                    "."
            );

            notification.setCreatedAt(
                    createDateTime(
                            request.getInterviewDate(),
                            request.getInterviewTime()
                    )
            );

            notification.setTargetPage("admin-interviews.html");

            notifications.add(notification);
        }
    }

    /* ================= HELPERS ================= */

    private String normalizeAdminUsername(String adminUsername) {
        if (adminUsername == null) {
            return "";
        }

        return adminUsername.trim().toLowerCase();
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value.trim();
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (Exception error) {
            return null;
        }
    }

    private LocalDateTime createDateTime(
            String dateValue,
            String timeValue
    ) {
        try {
            LocalDate date = LocalDate.parse(dateValue);

            if (timeValue == null || timeValue.isBlank()) {
                return date.atStartOfDay();
            }

            LocalTime time = LocalTime.parse(timeValue);

            return LocalDateTime.of(date, time);

        } catch (Exception error) {
            return LocalDateTime.now();
        }
    }

    private String formatDate(String value) {
        try {
            LocalDate date = LocalDate.parse(value);

            return date.getDayOfMonth() + " " +
                    date.getMonth().toString().substring(0, 1) +
                    date.getMonth().toString().substring(1)
                            .toLowerCase() +
                    " " +
                    date.getYear();

        } catch (Exception error) {
            return value;
        }
    }
}