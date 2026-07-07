(function () {
    const API_BASE = "/api/admin-settings";

    const PROFILE_KEY = "recruiteaseAdminProfile";
    const PHOTO_KEY = "recruiteaseAdminPhoto";
    const THEME_KEY = "recruiteaseAdminTheme";
    const DEFAULT_PHOTO = "https://i.pravatar.cc/80?img=12";

    const state = {
        adminUsername: String(
            localStorage.getItem("adminUsername") || ""
        ).trim(),

        adminRole: String(
            localStorage.getItem("adminRole") || ""
        ).trim(),

        portalSettings: null,
        adminProfile: null
    };

    const allPanels = {
        general: "generalPanel",
        users: "usersPanel",
        email: "emailPanel",
        notifications: "notificationsPanel",
        system: "systemPanel",
        backup: "backupPanel"
    };

    /* ================= COMMON HELPERS ================= */

    function getElement(id) {
        return document.getElementById(id);
    }

    function setValue(id, value) {
        const element = getElement(id);

        if (element) {
            element.value = value ?? "";
        }
    }

    function setChecked(id, value) {
        const element = getElement(id);

        if (element) {
            element.checked = Boolean(value);
        }
    }

    function clean(value) {
        return String(value ?? "").trim();
    }

    function isMainAdmin() {
        return state.adminRole.toUpperCase() === "MAIN_ADMIN";
    }

    function getErrorMessage(data, fallbackMessage) {
        if (typeof data === "string" && data.trim() !== "") {
            return data;
        }

        if (data && typeof data.message === "string") {
            return data.message;
        }

        return fallbackMessage;
    }

    async function apiRequest(url, options = {}) {
        const requestOptions = {
            ...options,
            headers: {
                ...(options.headers || {})
            }
        };

        if (options.body) {
            requestOptions.headers["Content-Type"] = "application/json";
        }

        const response = await fetch(url, requestOptions);

        const responseText = await response.text();

        let responseData = null;

        try {
            responseData = responseText
                ? JSON.parse(responseText)
                : null;
        } catch (error) {
            responseData = responseText;
        }

        if (!response.ok) {
            throw new Error(
                getErrorMessage(responseData, "Request failed.")
            );
        }

        return responseData;
    }

    function profileUrl(path) {
        return (
            API_BASE +
            path +
            "?adminUsername=" +
            encodeURIComponent(state.adminUsername)
        );
    }

    function portalUrl(path) {
        return (
            API_BASE +
            path +
            "?adminRole=" +
            encodeURIComponent(state.adminRole)
        );
    }

    function requireMainAdmin() {
        if (isMainAdmin()) {
            return true;
        }

        alert("Only Main Admin can change portal-wide settings.");
        return false;
    }

    /* ================= PAGE SECURITY ================= */

    function validateAdminSession() {
        if (state.adminUsername && state.adminRole) {
            return true;
        }

        alert("Please login as an approved admin first.");
        window.location.href = "admin-login.html";

        return false;
    }

    /* ================= TAB FUNCTIONS ================= */

    function switchTab(tabName, button) {
        const panelId = allPanels[tabName];

        if (!panelId) {
            return;
        }

        Object.values(allPanels).forEach(function (id) {
            const panel = getElement(id);

            if (panel) {
                panel.style.display = "none";
            }
        });

        const selectedPanel = getElement(panelId);

        if (selectedPanel) {
            selectedPanel.style.display = "block";
        }

        document.querySelectorAll(".settings-tabs button").forEach(
            function (tabButton) {
                tabButton.classList.remove("active");
            }
        );

        if (button) {
            button.classList.add("active");
        }

        if (tabName === "users" && isMainAdmin()) {
            loadPortalUsers();
        }
    }

    function switchTabByName(tabName) {
        const button = document.querySelector(
            `.settings-tabs button[data-tab="${tabName}"]`
        );

        if (!button) {
            alert("This section is available only for Main Admin.");
            return;
        }

        switchTab(tabName, button);

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });
    }

    /* ================= LOAD PORTAL SETTINGS ================= */

    async function loadPortalSettings() {
        try {
            const settings = await apiRequest(
                API_BASE + "/portal"
            );

            state.portalSettings = settings;

            setValue("instituteName", settings.instituteName);
            setValue("instituteCode", settings.instituteCode);
            setValue("instituteAddress", settings.instituteAddress);
            setValue("contactEmail", settings.contactEmail);
            setValue("contactPhone", settings.contactPhone);
            setValue("website", settings.website);

            setValue("senderName", settings.senderName);
            setValue("senderEmail", settings.senderEmail);
            setValue("smtpHost", settings.smtpHost);
            setValue("smtpPort", settings.smtpPort || 587);
            setValue("smtpEncryption", settings.smtpEncryption || "TLS");
            setValue("replyToEmail", settings.replyToEmail);

            setChecked(
                "notifyStudentRegister",
                settings.notifyStudentRegister
            );

            setChecked(
                "notifyCompanyAdd",
                settings.notifyCompanyAdd
            );

            setChecked(
                "notifyApplication",
                settings.notifyApplication
            );

            setChecked(
                "notifyTest",
                settings.notifyTest
            );

            setChecked(
                "notifyPlacement",
                settings.notifyPlacement
            );

            setValue("placementSession", settings.placementSession);
            setValue(
                "defaultApplicationStatus",
                settings.defaultApplicationStatus || "Applied"
            );

            setValue(
                "maxResumeSize",
                settings.maxResumeSize || 5
            );

            setValue(
                "studentRegistrationMode",
                settings.studentRegistrationMode || "Open"
            );

            setValue(
                "systemAnnouncement",
                settings.systemAnnouncement
            );

        } catch (error) {
            console.error(error);
            alert("Portal settings load nahi ho rahi: " + error.message);
        }
    }

    /* ================= SAVE GENERAL SETTINGS ================= */

    async function saveGeneralSettings() {
        if (!requireMainAdmin()) {
            return;
        }

        const payload = {
            instituteName: clean(getElement("instituteName").value),
            instituteCode: clean(getElement("instituteCode").value),
            instituteAddress: clean(getElement("instituteAddress").value),
            contactEmail: clean(getElement("contactEmail").value),
            contactPhone: clean(getElement("contactPhone").value),
            website: clean(getElement("website").value)
        };

        if (!payload.instituteName || !payload.instituteCode) {
            alert("Institute Name aur Institute Code required hain.");
            return;
        }

        try {
            const savedSettings = await apiRequest(
                portalUrl("/portal/general"),
                {
                    method: "PUT",
                    body: JSON.stringify(payload)
                }
            );

            state.portalSettings = savedSettings;

            alert("General settings database me save ho gayi.");

        } catch (error) {
            console.error(error);
            alert("General settings save nahi hui: " + error.message);
        }
    }

    /* ================= ADMIN PROFILE ================= */

    async function loadAdminProfile() {
        try {
            const profile = await apiRequest(
                profileUrl("/profile")
            );

            state.adminProfile = profile;

            applyAdminProfile(profile);
            applyAdminPhoto(profile.photoData || DEFAULT_PHOTO);

        } catch (error) {
            console.error(error);
            alert("Admin profile load nahi ho raha: " + error.message);
        }
    }

    function applyAdminProfile(profile) {
        const fullName = profile.fullName || "Admin";
        const email = profile.email || "";
        const phoneNumber = profile.phoneNumber || "";

        setValue("adminName", fullName);
        setValue("adminEmail", email);
        setValue("adminPhone", phoneNumber);

        const profileDisplayName = getElement("profileDisplayName");
        const profileDisplayEmail = getElement("profileDisplayEmail");
        const topAdminName = getElement("topAdminName");

        if (profileDisplayName) {
            profileDisplayName.innerText = fullName;
        }

        if (profileDisplayEmail) {
            profileDisplayEmail.innerText = email || "Not set";
        }

        if (topAdminName) {
            topAdminName.innerText = fullName;
        }

        const localProfile = {
            name: fullName,
            email: email,
            phone: phoneNumber
        };

        localStorage.setItem(
            PROFILE_KEY,
            JSON.stringify(localProfile)
        );
    }

    function applyAdminPhoto(photoData) {
        const safePhoto = photoData || DEFAULT_PHOTO;

        const profilePhoto = getElement("adminProfilePhoto");
        const topPhoto = getElement("topAdminPhoto");

        if (profilePhoto) {
            profilePhoto.src = safePhoto;
        }

        if (topPhoto) {
            topPhoto.src = safePhoto;
        }

        localStorage.setItem(PHOTO_KEY, safePhoto);
    }

    async function updateAdminProfile(showMessage = true) {
        const fullName = clean(getElement("adminName").value);
        const email = clean(getElement("adminEmail").value);
        const phoneNumber = clean(getElement("adminPhone").value);

        if (!fullName || !email || !phoneNumber) {
            alert("Name, email aur phone required hain.");
            return;
        }

        const payload = {
            fullName: fullName,
            email: email,
            phoneNumber: phoneNumber,
            photoData: state.adminProfile
                ? state.adminProfile.photoData
                : ""
        };

        try {
            const savedProfile = await apiRequest(
                profileUrl("/profile"),
                {
                    method: "PUT",
                    body: JSON.stringify(payload)
                }
            );

            state.adminProfile = savedProfile;

            applyAdminProfile(savedProfile);
            applyAdminPhoto(
                savedProfile.photoData || DEFAULT_PHOTO
            );

            if (showMessage) {
                alert("Admin profile database me update ho gaya.");
            }

        } catch (error) {
            console.error(error);
            alert("Admin profile update nahi hua: " + error.message);
        }
    }

    /* ================= ADMIN PHOTO ================= */

    function bindAdminPhotoUpload() {
        const photoUpload = getElement("adminPhotoUpload");

        if (!photoUpload) {
            return;
        }

        photoUpload.addEventListener("change", function () {
            const file = this.files[0];

            if (!file) {
                return;
            }

            if (!file.type.startsWith("image/")) {
                alert("Please select a valid image file.");
                photoUpload.value = "";
                return;
            }

            const reader = new FileReader();

            reader.onload = function (event) {
                const image = new Image();

                image.onload = async function () {
                    const canvas = document.createElement("canvas");
                    const context = canvas.getContext("2d");

                    const outputSize = 220;
                    const sourceSize = Math.min(
                        image.width,
                        image.height
                    );

                    const sourceX =
                        (image.width - sourceSize) / 2;

                    const sourceY =
                        (image.height - sourceSize) / 2;

                    canvas.width = outputSize;
                    canvas.height = outputSize;

                    context.drawImage(
                        image,
                        sourceX,
                        sourceY,
                        sourceSize,
                        sourceSize,
                        0,
                        0,
                        outputSize,
                        outputSize
                    );

                    const photoData = canvas.toDataURL(
                        "image/jpeg",
                        0.72
                    );

                    if (!state.adminProfile) {
                        state.adminProfile = {};
                    }

                    state.adminProfile.photoData = photoData;

                    applyAdminPhoto(photoData);

                    await updateAdminProfile(false);

                    alert("Admin photo database me save ho gayi.");

                    photoUpload.value = "";
                };

                image.onerror = function () {
                    alert("Image load nahi ho rahi.");
                    photoUpload.value = "";
                };

                image.src = event.target.result;
            };

            reader.readAsDataURL(file);
        });
    }

    /* ================= CHANGE PASSWORD ================= */

    async function updatePassword() {
        const currentPassword = getElement("currentPassword").value;
        const newPassword = getElement("newPassword").value;
        const confirmPassword = getElement("confirmPassword").value;

        if (!currentPassword || !newPassword || !confirmPassword) {
            alert("Please fill all password fields.");
            return;
        }

        if (newPassword !== confirmPassword) {
            alert("New password aur confirm password match nahi kar rahe.");
            return;
        }

        if (newPassword.length < 6) {
            alert("New password minimum 6 characters ka hona chahiye.");
            return;
        }

        try {
            const response = await apiRequest(
                profileUrl("/profile/change-password"),
                {
                    method: "POST",
                    body: JSON.stringify({
                        currentPassword: currentPassword,
                        newPassword: newPassword
                    })
                }
            );

            getElement("currentPassword").value = "";
            getElement("newPassword").value = "";
            getElement("confirmPassword").value = "";

            alert(
                response.message ||
                "Password updated successfully."
            );

        } catch (error) {
            console.error(error);
            alert("Password update nahi hua: " + error.message);
        }
    }

    /* ================= PREFERENCES ================= */

    function fillPreferences(profile) {
        setValue("language", profile.language || "English");

        setValue(
            "timezone",
            profile.timezone || "GMT+05:30 Asia/Kolkata"
        );

        setChecked(
            "darkModePreference",
            profile.darkModePreference
        );

        setChecked(
            "emailNotifications",
            profile.emailNotifications
        );

        setChecked(
            "smsNotifications",
            profile.smsNotifications
        );

        setChecked(
            "analyticsPreference",
            profile.analyticsPreference
        );
    }

    async function savePreferences() {
        const payload = {
            language: getElement("language").value,
            timezone: getElement("timezone").value,
            darkModePreference:
                getElement("darkModePreference").checked,
            emailNotifications:
                getElement("emailNotifications").checked,
            smsNotifications:
                getElement("smsNotifications").checked,
            analyticsPreference:
                getElement("analyticsPreference").checked
        };

        try {
            const savedProfile = await apiRequest(
                profileUrl("/profile/preferences"),
                {
                    method: "PUT",
                    body: JSON.stringify(payload)
                }
            );

            state.adminProfile = savedProfile;
            fillPreferences(savedProfile);

            const selectedTheme =
                savedProfile.darkModePreference
                    ? "dark"
                    : "light";

            localStorage.setItem(THEME_KEY, selectedTheme);

            document.body.classList.toggle(
                "recruitease-light-mode",
                selectedTheme === "light"
            );

            const themeButton = getElement("themeToggle");

            if (themeButton) {
                themeButton.innerText =
                    selectedTheme === "light"
                        ? "☀️"
                        : "🌙";
            }

            alert("Preferences database me save ho gayi.");

        } catch (error) {
            console.error(error);
            alert("Preferences save nahi hui: " + error.message);
        }
    }

    /* ================= USER MANAGEMENT ================= */

    async function loadPortalUsers() {
        if (!isMainAdmin()) {
            return;
        }

        const table = getElement("adminUsersTable");

        if (table) {
            table.innerHTML = `
                <tr>
                    <td colspan="5" style="text-align:center;padding:18px;">
                        Loading users...
                    </td>
                </tr>
            `;
        }

        try {
            const users = await apiRequest(
                portalUrl("/users")
            );

            renderPortalUsers(users);

        } catch (error) {
            console.error(error);

            if (table) {
                table.innerHTML = `
                    <tr>
                        <td colspan="5" style="text-align:center;padding:18px;">
                            Users load nahi ho rahe.
                        </td>
                    </tr>
                `;
            }
        }
    }

    function renderPortalUsers(users) {
        const table = getElement("adminUsersTable");

        if (!table) {
            return;
        }

        if (!Array.isArray(users) || users.length === 0) {
            table.innerHTML = `
                <tr>
                    <td colspan="5" style="text-align:center;padding:18px;">
                        No admin users found.
                    </td>
                </tr>
            `;
            return;
        }

        table.innerHTML = users.map(function (user, index) {
            return `
                <tr>
                    <td>${index + 1}</td>
                    <td>${escapeHtml(user.name || "Admin")}</td>
                    <td>${escapeHtml(user.email || "-")}</td>
                    <td>${escapeHtml(user.role || "Admin")}</td>
                    <td>${escapeHtml(user.status || "Active")}</td>
                </tr>
            `;
        }).join("");
    }

    async function addNewAdmin() {
        if (!requireMainAdmin()) {
            return;
        }

        const fullName = clean(getElement("newAdminName").value);
        const email = clean(getElement("newAdminEmail").value);
        const role = getElement("newAdminRole").value;
        const password = clean(getElement("newAdminPassword").value);

        if (!fullName || !email || !password) {
            alert("Name, email aur temporary password required hain.");
            return;
        }

        if (password.length < 6) {
            alert("Temporary password minimum 6 characters ka hona chahiye.");
            return;
        }

        try {
            const createdUser = await apiRequest(
                portalUrl("/users"),
                {
                    method: "POST",
                    body: JSON.stringify({
                        fullName: fullName,
                        email: email,
                        role: role,
                        password: password
                    })
                }
            );

            getElement("newAdminName").value = "";
            getElement("newAdminEmail").value = "";
            getElement("newAdminPassword").value = "";

            await loadPortalUsers();

            alert(
                "Admin added successfully.\n\nUsername: " +
                createdUser.username
            );

        } catch (error) {
            console.error(error);
            alert("New admin add nahi hua: " + error.message);
        }
    }

    /* ================= EMAIL SETTINGS ================= */

    async function saveEmailSettings() {
        if (!requireMainAdmin()) {
            return;
        }

        const smtpPort = Number(
            getElement("smtpPort").value
        );

        const payload = {
            senderName: clean(getElement("senderName").value),
            senderEmail: clean(getElement("senderEmail").value),
            smtpHost: clean(getElement("smtpHost").value),
            smtpPort: smtpPort > 0 ? smtpPort : 587,
            smtpEncryption: getElement("smtpEncryption").value,
            replyToEmail: clean(getElement("replyToEmail").value)
        };

        try {
            const savedSettings = await apiRequest(
                portalUrl("/portal/email"),
                {
                    method: "PUT",
                    body: JSON.stringify(payload)
                }
            );

            state.portalSettings = savedSettings;

            alert(
                "Sender details database me save ho gayi.\n\n" +
                "SMTP/Gmail App Password application configuration me safe rahega."
            );

        } catch (error) {
            console.error(error);
            alert("Email settings save nahi hui: " + error.message);
        }
    }

    function sendTestEmail() {
        alert(
            "Sender details save ho sakti hain.\n\n" +
            "Test Email API abhi add nahi ki gayi, kyunki existing Gmail SMTP configuration ko runtime settings se overwrite nahi karna chahiye."
        );
    }

    /* ================= NOTIFICATION SETTINGS ================= */

    async function saveNotificationSettings() {
        if (!requireMainAdmin()) {
            return;
        }

        const payload = {
            notifyStudentRegister:
                getElement("notifyStudentRegister").checked,

            notifyCompanyAdd:
                getElement("notifyCompanyAdd").checked,

            notifyApplication:
                getElement("notifyApplication").checked,

            notifyTest:
                getElement("notifyTest").checked,

            notifyPlacement:
                getElement("notifyPlacement").checked
        };

        try {
            const savedSettings = await apiRequest(
                portalUrl("/portal/notifications"),
                {
                    method: "PUT",
                    body: JSON.stringify(payload)
                }
            );

            state.portalSettings = savedSettings;

            alert("Notification settings database me save ho gayi.");

        } catch (error) {
            console.error(error);
            alert(
                "Notification settings save nahi hui: " +
                error.message
            );
        }
    }

    /* ================= SYSTEM SETTINGS ================= */

    async function saveSystemSettings() {
        if (!requireMainAdmin()) {
            return;
        }

        const maxResumeSize = Number(
            getElement("maxResumeSize").value
        );

        if (!maxResumeSize || maxResumeSize <= 0) {
            alert("Maximum Resume Size valid number hona chahiye.");
            return;
        }

        const payload = {
            placementSession:
                clean(getElement("placementSession").value),

            defaultApplicationStatus:
                getElement("defaultApplicationStatus").value,

            maxResumeSize: maxResumeSize,

            studentRegistrationMode:
                getElement("studentRegistrationMode").value,

            systemAnnouncement:
                clean(getElement("systemAnnouncement").value)
        };

        try {
            const savedSettings = await apiRequest(
                portalUrl("/portal/system"),
                {
                    method: "PUT",
                    body: JSON.stringify(payload)
                }
            );

            state.portalSettings = savedSettings;

            alert("System settings database me save ho gayi.");

        } catch (error) {
            console.error(error);
            alert("System settings save nahi hui: " + error.message);
        }
    }

    /* ================= BACKUP ================= */

    async function createBackup() {
        try {
            const responses = await Promise.all([
                apiRequest("/api/students"),
                apiRequest("/api/companies"),
                apiRequest("/api/applications/all"),
                apiRequest("/api/aptitude-tests"),
                apiRequest(API_BASE + "/portal")
            ]);

            const backupData = {
                createdAt: new Date().toLocaleString("en-IN"),
                portalSettings: responses[4],
                students: responses[0],
                companies: responses[1],
                applications: responses[2],
                aptitudeTests: responses[3]
            };

            const backupFile = new Blob(
                [JSON.stringify(backupData, null, 2)],
                {
                    type: "application/json"
                }
            );

            const fileUrl = URL.createObjectURL(backupFile);

            const link = document.createElement("a");
            link.href = fileUrl;
            link.download = "recruitease-backup.json";

            document.body.appendChild(link);
            link.click();
            link.remove();

            URL.revokeObjectURL(fileUrl);

            setValue(
                "lastBackup",
                "Backup created: " +
                new Date().toLocaleString("en-IN")
            );

        } catch (error) {
            console.error(error);
            alert("Backup create nahi hua: " + error.message);
        }
    }

    function restoreBackup() {
        const restoreFile = getElement("restoreFile");
        const file = restoreFile ? restoreFile.files[0] : null;

        if (!file) {
            alert("Pehle backup file select karo.");
            return;
        }

        alert(
            "Backup file selected hai.\n\n" +
            "Database restore API abhi add nahi ki gayi, isliye existing production data safe rahega."
        );
    }

    /* ================= SYSTEM OVERVIEW ================= */

    async function loadSystemOverview() {
        try {
            const results = await Promise.all([
                apiRequest("/api/students"),
                apiRequest("/api/companies"),
                apiRequest("/api/drives"),
                apiRequest("/api/applications/all")
            ]);

            const students = results[0];
            const companies = results[1];
            const drives = results[2];
            const applications = results[3];

            const placedStudents = new Set();

            applications.forEach(function (application) {
                const status = String(
                    application.status || ""
                ).toLowerCase();

                if (
                    (status === "placed" || status === "selected") &&
                    application.studentId
                ) {
                    placedStudents.add(application.studentId);
                }
            });

            const overviewStudents = getElement("overviewStudents");
            const overviewCompanies = getElement("overviewCompanies");
            const overviewDrives = getElement("overviewDrives");
            const overviewApplications = getElement("overviewApplications");
            const overviewPlaced = getElement("overviewPlaced");

            if (overviewStudents) {
                overviewStudents.innerText = students.length;
            }

            if (overviewCompanies) {
                overviewCompanies.innerText = companies.length;
            }

            if (overviewDrives) {
                overviewDrives.innerText = drives.length;
            }

            if (overviewApplications) {
                overviewApplications.innerText =
                    applications.length;
            }

            if (overviewPlaced) {
                overviewPlaced.innerText =
                    placedStudents.size;
            }

        } catch (error) {
            console.error(error);
        }
    }

    /* ================= QUICK ACTIONS ================= */

    function showSystemLogs() {
        alert(
            "System Logs\n\n" +
            "• Admin Settings database API active\n" +
            "• Current admin: " + state.adminUsername + "\n" +
            "• Current role: " + state.adminRole
        );
    }

    function clearCache() {
        const confirmed = confirm(
            "Clear local browser cache?\n\n" +
            "Database data delete nahi hoga."
        );

        if (!confirmed) {
            return;
        }

        localStorage.removeItem(PROFILE_KEY);
        localStorage.removeItem(PHOTO_KEY);

        alert(
            "Browser cache clear ho gaya.\n" +
            "Page refresh par real database profile dobara load hoga."
        );

        window.location.reload();
    }

    /* ================= SEARCH ================= */

    function searchSettings(value) {
        const searchValue = String(value || "")
            .toLowerCase()
            .trim();

        if (searchValue.includes("email")) {
            switchTabByName("email");

        } else if (
            searchValue.includes("user") ||
            searchValue.includes("admin") ||
            searchValue.includes("role")
        ) {
            switchTabByName("users");

        } else if (searchValue.includes("notification")) {
            switchTabByName("notifications");

        } else if (searchValue.includes("system")) {
            switchTabByName("system");

        } else if (searchValue.includes("backup")) {
            switchTabByName("backup");

        } else if (
            searchValue.includes("general") ||
            searchValue.includes("institute")
        ) {
            switchTabByName("general");
        }
    }

    /* ================= ROLE ACCESS ================= */

    function applyRoleAccess() {
        if (isMainAdmin()) {
            return;
        }

        const mainAdminOnlyTabs = [
            "users",
            "email",
            "notifications",
            "system"
        ];

        mainAdminOnlyTabs.forEach(function (tabName) {
            const tab = document.querySelector(
                `.settings-tabs button[data-tab="${tabName}"]`
            );

            if (tab) {
                tab.style.display = "none";
            }
        });

        const generalInputs = [
            "instituteName",
            "instituteCode",
            "instituteAddress",
            "contactEmail",
            "contactPhone",
            "website"
        ];

        generalInputs.forEach(function (id) {
            const input = getElement(id);

            if (input) {
                input.disabled = true;
            }
        });

        document.querySelectorAll(
            '#generalPanel button[onclick="saveGeneralSettings()"]'
        ).forEach(function (button) {
            button.style.display = "none";
        });
    }

    /* ================= LOGOUT ================= */

    
       function adminLogout() {
        localStorage.removeItem("adminUsername");
        localStorage.removeItem("adminRole");
        localStorage.removeItem("adminToken");

        window.location.href = "admin-login.html";
  }

    /* ================= NOTIFICATION PLACEHOLDER ================= */

    function showNotifications() {
        window.location.href = "admin-dashboard.html";
    }

    /* ================= ESCAPE HTML ================= */

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    /* ================= INITIAL LOAD ================= */

    async function initializeSettingsPage() {
        if (!validateAdminSession()) {
            return;
        }

        const lastLogin = getElement("lastLogin");

        if (lastLogin) {
            lastLogin.innerText =
                new Date().toLocaleString("en-IN");
        }

        bindAdminPhotoUpload();
        applyRoleAccess();

        await Promise.all([
            loadPortalSettings(),
            loadAdminProfile(),
            loadSystemOverview()
        ]);

        if (isMainAdmin()) {
            loadPortalUsers();
        }
    }

    /* ================= EXPOSE FUNCTIONS FOR HTML ================= */

    window.switchTab = switchTab;
    window.switchTabByName = switchTabByName;

    window.saveGeneralSettings = saveGeneralSettings;
    window.updateAdminProfile = updateAdminProfile;
    window.updatePassword = updatePassword;
    window.savePreferences = savePreferences;

    window.addNewAdmin = addNewAdmin;
    window.saveEmailSettings = saveEmailSettings;
    window.sendTestEmail = sendTestEmail;

    window.saveNotificationSettings = saveNotificationSettings;
    window.saveSystemSettings = saveSystemSettings;

    window.createBackup = createBackup;
    window.restoreBackup = restoreBackup;

    window.showSystemLogs = showSystemLogs;
    window.clearCache = clearCache;
    window.searchSettings = searchSettings;

    window.adminLogout = adminLogout;
    window.showNotifications = showNotifications;

    document.addEventListener(
        "DOMContentLoaded",
        initializeSettingsPage
    );
})();