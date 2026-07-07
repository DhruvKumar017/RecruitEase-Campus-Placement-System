package com.recruitease.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.recruitease.entity.AdminProfile;
import com.recruitease.entity.AdminRequest;
import com.recruitease.entity.MainAdminCredential;
import com.recruitease.entity.PortalSettings;
import com.recruitease.repository.AdminProfileRepository;
import com.recruitease.repository.AdminRequestRepository;
import com.recruitease.repository.MainAdminCredentialRepository;
import com.recruitease.repository.PortalSettingsRepository;

@RestController
@RequestMapping("/api/admin-settings")
public class AdminSettingsController {

    private final PortalSettingsRepository portalSettingsRepository;
    private final AdminProfileRepository adminProfileRepository;
    private final MainAdminCredentialRepository mainAdminCredentialRepository;
    private final AdminRequestRepository adminRequestRepository;

    @Value("${app.main-admin.username}")
    private String mainAdminUsername;

    @Value("${app.main-admin.password}")
    private String mainAdminPassword;

    @Value("${app.main-admin.email}")
    private String mainAdminEmail;

    public AdminSettingsController(
            PortalSettingsRepository portalSettingsRepository,
            AdminProfileRepository adminProfileRepository,
            MainAdminCredentialRepository mainAdminCredentialRepository,
            AdminRequestRepository adminRequestRepository
    ) {
        this.portalSettingsRepository = portalSettingsRepository;
        this.adminProfileRepository = adminProfileRepository;
        this.mainAdminCredentialRepository = mainAdminCredentialRepository;
        this.adminRequestRepository = adminRequestRepository;
    }

    /* =====================================================
       PORTAL SETTINGS
       Main Admin only can update global portal settings.
       ===================================================== */

    @GetMapping("/portal")
    public PortalSettings getPortalSettings() {
        return getOrCreatePortalSettings();
    }

    @PutMapping("/portal/general")
    public ResponseEntity<?> updateGeneralSettings(
            @RequestParam String adminRole,
            @RequestBody PortalSettings input
    ) {
        if (!isMainAdmin(adminRole)) {
            return forbiddenMainAdminOnly();
        }

        if (isBlank(input.getInstituteName()) ||
                isBlank(input.getInstituteCode())) {

            return ResponseEntity.badRequest().body(
                    "Institute name and institute code are required."
            );
        }

        PortalSettings settings = getOrCreatePortalSettings();

        settings.setInstituteName(clean(input.getInstituteName()));
        settings.setInstituteCode(clean(input.getInstituteCode()));
        settings.setInstituteAddress(clean(input.getInstituteAddress()));
        settings.setContactEmail(clean(input.getContactEmail()));
        settings.setContactPhone(clean(input.getContactPhone()));
        settings.setWebsite(clean(input.getWebsite()));

        return ResponseEntity.ok(
                portalSettingsRepository.save(settings)
        );
    }

    @PutMapping("/portal/email")
    public ResponseEntity<?> updateEmailSettings(
            @RequestParam String adminRole,
            @RequestBody PortalSettings input
    ) {
        if (!isMainAdmin(adminRole)) {
            return forbiddenMainAdminOnly();
        }

        PortalSettings settings = getOrCreatePortalSettings();

        settings.setSenderName(clean(input.getSenderName()));
        settings.setSenderEmail(clean(input.getSenderEmail()));
        settings.setSmtpHost(clean(input.getSmtpHost()));
        settings.setSmtpPort(
                input.getSmtpPort() == null || input.getSmtpPort() <= 0
                        ? 587
                        : input.getSmtpPort()
        );
        settings.setSmtpEncryption(
                isBlank(input.getSmtpEncryption())
                        ? "TLS"
                        : clean(input.getSmtpEncryption())
        );
        settings.setReplyToEmail(clean(input.getReplyToEmail()));

        return ResponseEntity.ok(
                portalSettingsRepository.save(settings)
        );
    }

    @PutMapping("/portal/notifications")
    public ResponseEntity<?> updateNotificationSettings(
            @RequestParam String adminRole,
            @RequestBody PortalSettings input
    ) {
        if (!isMainAdmin(adminRole)) {
            return forbiddenMainAdminOnly();
        }

        PortalSettings settings = getOrCreatePortalSettings();

        settings.setNotifyStudentRegister(
                input.isNotifyStudentRegister()
        );
        settings.setNotifyCompanyAdd(
                input.isNotifyCompanyAdd()
        );
        settings.setNotifyApplication(
                input.isNotifyApplication()
        );
        settings.setNotifyTest(
                input.isNotifyTest()
        );
        settings.setNotifyPlacement(
                input.isNotifyPlacement()
        );

        return ResponseEntity.ok(
                portalSettingsRepository.save(settings)
        );
    }

    @PutMapping("/portal/system")
    public ResponseEntity<?> updateSystemSettings(
            @RequestParam String adminRole,
            @RequestBody PortalSettings input
    ) {
        if (!isMainAdmin(adminRole)) {
            return forbiddenMainAdminOnly();
        }

        if (input.getMaxResumeSize() == null ||
                input.getMaxResumeSize() <= 0) {

            return ResponseEntity.badRequest().body(
                    "Maximum resume size must be greater than 0."
            );
        }

        PortalSettings settings = getOrCreatePortalSettings();

        settings.setPlacementSession(clean(input.getPlacementSession()));
        settings.setDefaultApplicationStatus(
                clean(input.getDefaultApplicationStatus())
        );
        settings.setMaxResumeSize(input.getMaxResumeSize());
        settings.setStudentRegistrationMode(
                clean(input.getStudentRegistrationMode())
        );
        settings.setSystemAnnouncement(
                clean(input.getSystemAnnouncement())
        );

        return ResponseEntity.ok(
                portalSettingsRepository.save(settings)
        );
    }

    /* =====================================================
       ADMIN PROFILE + PREFERENCES
       Main Admin and Approved Admin both can use these.
       ===================================================== */

    @GetMapping("/profile")
    public ResponseEntity<?> getAdminProfile(
            @RequestParam String adminUsername
    ) {
        String username = normalizeUsername(adminUsername);

        if (username.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Admin username is required.");
        }

        return ResponseEntity.ok(
                getOrCreateAdminProfile(username)
        );
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateAdminProfile(
            @RequestParam String adminUsername,
            @RequestBody AdminProfile input
    ) {
        String username = normalizeUsername(adminUsername);

        if (username.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Admin username is required.");
        }

        if (isBlank(input.getFullName()) ||
                isBlank(input.getEmail()) ||
                isBlank(input.getPhoneNumber())) {

            return ResponseEntity.badRequest()
                    .body("Name, email and phone are required.");
        }

        AdminProfile profile = getOrCreateAdminProfile(username);

        profile.setFullName(clean(input.getFullName()));
        profile.setEmail(clean(input.getEmail()));
        profile.setPhoneNumber(clean(input.getPhoneNumber()));

        if (!isBlank(input.getPhotoData())) {
            profile.setPhotoData(input.getPhotoData());
        }

        AdminProfile savedProfile =
                adminProfileRepository.save(profile);

        syncApprovedAdminContactDetails(username, savedProfile);

        return ResponseEntity.ok(savedProfile);
    }

    @PutMapping("/profile/preferences")
    public ResponseEntity<?> updateAdminPreferences(
            @RequestParam String adminUsername,
            @RequestBody AdminProfile input
    ) {
        String username = normalizeUsername(adminUsername);

        if (username.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Admin username is required.");
        }

        AdminProfile profile = getOrCreateAdminProfile(username);

        profile.setLanguage(
                isBlank(input.getLanguage())
                        ? "English"
                        : clean(input.getLanguage())
        );

        profile.setTimezone(
                isBlank(input.getTimezone())
                        ? "GMT+05:30 Asia/Kolkata"
                        : clean(input.getTimezone())
        );

        profile.setDarkModePreference(
                input.isDarkModePreference()
        );

        profile.setEmailNotifications(
                input.isEmailNotifications()
        );

        profile.setSmsNotifications(
                input.isSmsNotifications()
        );

        profile.setAnalyticsPreference(
                input.isAnalyticsPreference()
        );

        return ResponseEntity.ok(
                adminProfileRepository.save(profile)
        );
    }

    @PostMapping("/profile/change-password")
    public ResponseEntity<?> changeAdminPassword(
            @RequestParam String adminUsername,
            @RequestBody Map<String, String> passwordData
    ) {
        String username = normalizeUsername(adminUsername);

        String currentPassword =
                clean(passwordData.get("currentPassword"));

        String newPassword =
                clean(passwordData.get("newPassword"));

        if (username.isEmpty() ||
                currentPassword.isEmpty() ||
                newPassword.isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Current password and new password are required.");
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                    .body("New password must contain at least 6 characters.");
        }

        MainAdminCredential mainAdmin =
                mainAdminCredentialRepository
                        .findTopByUsernameIgnoreCase(username)
                        .orElse(null);

        if (mainAdmin != null) {
            if (!currentPassword.equals(mainAdmin.getPassword())) {
                return ResponseEntity.badRequest()
                        .body("Current password is incorrect.");
            }

            mainAdmin.setPassword(newPassword);
            mainAdminCredentialRepository.save(mainAdmin);

            return ResponseEntity.ok(
                    Map.of("message", "Main admin password updated successfully.")
            );
        }

        if (username.equalsIgnoreCase(mainAdminUsername)) {
            if (!currentPassword.equals(mainAdminPassword)) {
                return ResponseEntity.badRequest()
                        .body("Current password is incorrect.");
            }

            MainAdminCredential newMainAdmin =
                    new MainAdminCredential();

            newMainAdmin.setUsername(mainAdminUsername);
            newMainAdmin.setEmail(mainAdminEmail);
            newMainAdmin.setPassword(newPassword);

            mainAdminCredentialRepository.save(newMainAdmin);

            return ResponseEntity.ok(
                    Map.of("message", "Main admin password updated successfully.")
            );
        }

        AdminRequest approvedAdmin =
                adminRequestRepository
                        .findByRequestedUsernameIgnoreCase(username)
                        .orElse(null);

        if (approvedAdmin == null ||
                !"Approved".equalsIgnoreCase(
                        approvedAdmin.getStatus()
                )) {

            return ResponseEntity.badRequest()
                    .body("Approved admin account not found.");
        }

        if (!currentPassword.equals(
                approvedAdmin.getRequestedPassword()
        )) {

            return ResponseEntity.badRequest()
                    .body("Current password is incorrect.");
        }

        approvedAdmin.setRequestedPassword(newPassword);
        adminRequestRepository.save(approvedAdmin);

        return ResponseEntity.ok(
                Map.of("message", "Admin password updated successfully.")
        );
    }

    /* =====================================================
       USER MANAGEMENT
       Main Admin only.
       ===================================================== */

    @GetMapping("/users")
    public ResponseEntity<?> getPortalUsers(
            @RequestParam String adminRole
    ) {
        if (!isMainAdmin(adminRole)) {
            return forbiddenMainAdminOnly();
        }

        List<Map<String, Object>> users = new ArrayList<>();
        Set<String> addedUsernames = new HashSet<>();

        for (MainAdminCredential mainAdmin :
                mainAdminCredentialRepository.findAll()) {

            String username =
                    normalizeUsername(mainAdmin.getUsername());

            if (username.isEmpty()) {
                continue;
            }

            users.add(
                    createUserMap(
                            mainAdmin.getId(),
                            username,
                            getProfileName(username, "Main Admin"),
                            mainAdmin.getEmail(),
                            "Main Admin",
                            "Active"
                    )
            );

            addedUsernames.add(username);
        }

        String configuredMainUsername =
                normalizeUsername(mainAdminUsername);

        if (!configuredMainUsername.isEmpty() &&
                !addedUsernames.contains(configuredMainUsername)) {

            users.add(
                    createUserMap(
                            0L,
                            configuredMainUsername,
                            getProfileName(
                                    configuredMainUsername,
                                    "Main Admin"
                            ),
                            mainAdminEmail,
                            "Main Admin",
                            "Active"
                    )
            );

            addedUsernames.add(configuredMainUsername);
        }

        List<AdminRequest> approvedAdmins =
                adminRequestRepository
                        .findByStatusIgnoreCase("Approved");

        for (AdminRequest admin : approvedAdmins) {
            String username =
                    normalizeUsername(
                            admin.getRequestedUsername()
                    );

            if (username.isEmpty() ||
                    addedUsernames.contains(username)) {
                continue;
            }

            users.add(
                    createUserMap(
                            admin.getId(),
                            username,
                            getProfileName(
                                    username,
                                    safeText(
                                            admin.getFullName(),
                                            "Admin"
                                    )
                            ),
                            admin.getEmail(),
                            safeText(
                                    admin.getDepartment(),
                                    "Admin"
                            ),
                            "Active"
                    )
            );

            addedUsernames.add(username);
        }

        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    public ResponseEntity<?> addApprovedAdmin(
            @RequestParam String adminRole,
            @RequestBody Map<String, String> adminData
    ) {
        if (!isMainAdmin(adminRole)) {
            return forbiddenMainAdminOnly();
        }

        String fullName = clean(adminData.get("fullName"));
        String email = clean(adminData.get("email"));
        String role = clean(adminData.get("role"));
        String password = clean(adminData.get("password"));

        if (fullName.isEmpty() ||
                email.isEmpty() ||
                password.isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Name, email and temporary password are required.");
        }

        if (!email.contains("@")) {
            return ResponseEntity.badRequest()
                    .body("Please enter a valid email address.");
        }

        if (password.length() < 6) {
            return ResponseEntity.badRequest()
                    .body("Temporary password must contain at least 6 characters.");
        }

        if (adminRequestRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.badRequest()
                    .body("This email is already used by an admin request.");
        }

        String generatedUsername =
                generateAvailableUsername(email);

        AdminRequest newAdmin = new AdminRequest();

        newAdmin.setFullName(fullName);
        newAdmin.setEmail(email);
        newAdmin.setPhoneNumber("");
        newAdmin.setRequestedUsername(generatedUsername);
        newAdmin.setRequestedPassword(password);
        newAdmin.setDepartment(
                role.isEmpty() ? "Admin" : role
        );
        newAdmin.setReason(
                "Added directly by Main Admin from Settings."
        );
        newAdmin.setStatus("Approved");

        AdminRequest savedAdmin =
                adminRequestRepository.save(newAdmin);

        AdminProfile profile = new AdminProfile();

        profile.setUsername(generatedUsername);
        profile.setFullName(fullName);
        profile.setEmail(email);
        profile.setPhoneNumber("");

        adminProfileRepository.save(profile);

        return ResponseEntity.ok(
                createUserMap(
                        savedAdmin.getId(),
                        generatedUsername,
                        fullName,
                        email,
                        role.isEmpty() ? "Admin" : role,
                        "Active"
                )
        );
    }

    /* =====================================================
       PRIVATE HELPER METHODS
       ===================================================== */

    private PortalSettings getOrCreatePortalSettings() {
        return portalSettingsRepository
                .findById(1L)
                .orElseGet(() -> {
                    PortalSettings settings = new PortalSettings();

                    settings.setId(1L);

                    return portalSettingsRepository.save(settings);
                });
    }

    private AdminProfile getOrCreateAdminProfile(
            String username
    ) {
        AdminProfile existingProfile =
                adminProfileRepository
                        .findByUsernameIgnoreCase(username)
                        .orElse(null);

        if (existingProfile != null) {
            return existingProfile;
        }

        AdminProfile profile = new AdminProfile();

        profile.setUsername(username);

        MainAdminCredential mainAdmin =
                mainAdminCredentialRepository
                        .findTopByUsernameIgnoreCase(username)
                        .orElse(null);

        if (mainAdmin != null) {
            profile.setFullName("Main Admin");
            profile.setEmail(mainAdmin.getEmail());
            profile.setPhoneNumber("");

            return adminProfileRepository.save(profile);
        }

        if (username.equalsIgnoreCase(mainAdminUsername)) {
            profile.setFullName("Main Admin");
            profile.setEmail(mainAdminEmail);
            profile.setPhoneNumber("");

            return adminProfileRepository.save(profile);
        }

        AdminRequest approvedAdmin =
                adminRequestRepository
                        .findByRequestedUsernameIgnoreCase(username)
                        .orElse(null);

        if (approvedAdmin != null) {
            profile.setFullName(
                    safeText(
                            approvedAdmin.getFullName(),
                            "Admin"
                    )
            );

            profile.setEmail(
                    safeText(
                            approvedAdmin.getEmail(),
                            ""
                    )
            );

            profile.setPhoneNumber(
                    safeText(
                            approvedAdmin.getPhoneNumber(),
                            ""
                    )
            );
        } else {
            profile.setFullName("Admin");
            profile.setEmail("");
            profile.setPhoneNumber("");
        }

        return adminProfileRepository.save(profile);
    }

    private void syncApprovedAdminContactDetails(
            String username,
            AdminProfile profile
    ) {
        AdminRequest approvedAdmin =
                adminRequestRepository
                        .findByRequestedUsernameIgnoreCase(username)
                        .orElse(null);

        if (approvedAdmin != null &&
                "Approved".equalsIgnoreCase(
                        approvedAdmin.getStatus()
                )) {

            approvedAdmin.setFullName(profile.getFullName());
            approvedAdmin.setEmail(profile.getEmail());
            approvedAdmin.setPhoneNumber(
                    profile.getPhoneNumber()
            );

            adminRequestRepository.save(approvedAdmin);
        }

        MainAdminCredential mainAdmin =
                mainAdminCredentialRepository
                        .findTopByUsernameIgnoreCase(username)
                        .orElse(null);

        if (mainAdmin != null) {
            mainAdmin.setEmail(profile.getEmail());
            mainAdminCredentialRepository.save(mainAdmin);
        }
    }

    private Map<String, Object> createUserMap(
            Long id,
            String username,
            String name,
            String email,
            String role,
            String status
    ) {
        Map<String, Object> user = new LinkedHashMap<>();

        user.put("id", id);
        user.put("username", username);
        user.put("name", name);
        user.put("email", email);
        user.put("role", role);
        user.put("status", status);

        return user;
    }

    private String getProfileName(
            String username,
            String fallback
    ) {
        return adminProfileRepository
                .findByUsernameIgnoreCase(username)
                .map(AdminProfile::getFullName)
                .filter(name -> !isBlank(name))
                .orElse(fallback);
    }

    private String generateAvailableUsername(String email) {
        String baseUsername = email
                .substring(0, email.indexOf("@"))
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");

        if (baseUsername.isEmpty()) {
            baseUsername = "admin";
        }

        String candidate = baseUsername;
        int number = 1;

        while (isUsernameTaken(candidate)) {
            candidate = baseUsername + number;
            number++;
        }

        return candidate;
    }

    private boolean isUsernameTaken(String username) {
        return mainAdminCredentialRepository
                .findTopByUsernameIgnoreCase(username)
                .isPresent()
                ||
                adminRequestRepository
                        .existsByRequestedUsernameIgnoreCase(
                                username
                        );
    }

    private boolean isMainAdmin(String adminRole) {
        return "MAIN_ADMIN".equalsIgnoreCase(
                clean(adminRole)
        );
    }

    private ResponseEntity<?> forbiddenMainAdminOnly() {
        return ResponseEntity.status(403).body(
                "Only Main Admin can change portal-wide settings."
        );
    }

    private String normalizeUsername(String username) {
        return clean(username).toLowerCase();
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeText(
            String value,
            String fallback
    ) {
        return isBlank(value) ? fallback : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}