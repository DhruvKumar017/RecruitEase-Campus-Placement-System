(function () {

    const studentId = localStorage.getItem("studentId");

    const menuItems = [
        {
            file: "student-dashboard.html",
            icon: "⌂",
            label: "Dashboard"
        },
        {
            file: "student-profile.html",
            icon: "●",
            label: "My Profile"
        },
        {
            file: "student-aptitude-tests.html",
            icon: "✓",
            label: "Aptitude Tests"
        },
        {
            file: "student-applications.html",
            icon: "▤",
            label: "My Applications"
        },
        {
            file: "student-interviews.html",
            icon: "●●",
            label: "Interviews"
        },
        {
            file: "student-resume.html",
            icon: "▧",
            label: "Resume"
        }
    ];

    function getCurrentFileName() {
        const path = window.location.pathname;
        const fileName = path.substring(path.lastIndexOf("/") + 1);

        return fileName || "student-dashboard.html";
    }

    function defaultAvatar() {
        return "data:image/svg+xml;charset=UTF-8," +
            encodeURIComponent(`
                <svg xmlns="http://www.w3.org/2000/svg"
                     width="220"
                     height="220">

                    <defs>
                        <linearGradient
                            id="avatarGradient"
                            x1="0%"
                            y1="0%"
                            x2="100%"
                            y2="100%"
                        >
                            <stop offset="0%" stop-color="#00d4ff"/>
                            <stop offset="100%" stop-color="#7c3aed"/>
                        </linearGradient>
                    </defs>

                    <rect
                        width="100%"
                        height="100%"
                        rx="110"
                        fill="url(#avatarGradient)"
                    />

                    <text
                        x="50%"
                        y="53%"
                        dominant-baseline="middle"
                        text-anchor="middle"
                        font-family="Arial"
                        font-size="70"
                        font-weight="bold"
                        fill="white"
                    >
                        ST
                    </text>

                </svg>
            `);
    }

    function loadSidebarCss() {

        const alreadyLoaded = document.querySelector(
            'link[href="student-sidebar.css"]'
        );

        if (alreadyLoaded) {
            return;
        }

        const css = document.createElement("link");

        css.rel = "stylesheet";
        css.href = "student-sidebar.css";

        document.head.appendChild(css);
    }

    function createSidebarHtml() {

        const currentFile = getCurrentFileName();

        const menuHtml = menuItems.map(function (item) {

            const activeClass =
                currentFile === item.file
                    ? "active"
                    : "";

            return `
                <a
                    class="student-unified-menu-item ${activeClass}"
                    href="${item.file}"
                >
                    <span class="student-menu-icon">
                        ${item.icon}
                    </span>

                    <span class="student-menu-label">
                        ${item.label}
                    </span>
                </a>
            `;

        }).join("");

        const notificationActive =
            currentFile === "student-notifications.html"
                ? "active"
                : "";

        const settingsActive =
            currentFile === "student-settings.html"
                ? "active"
                : "";

        return `
            <aside class="student-unified-sidebar">

                <div class="student-unified-brand">

                    <div class="student-unified-brand-icon">
                        ◆
                    </div>

                    <div>
                        <h2>
                            Recruit<span>Ease</span>
                        </h2>

                        <p>Student Placement Portal</p>
                    </div>

                </div>

                <div class="student-unified-user">

                    <img
                        id="unifiedSidebarPhoto"
                        src="${defaultAvatar()}"
                        alt="Student Photo"
                    >

                    <div class="student-sidebar-user-details">

                        <b id="unifiedSidebarName">
                            Student
                        </b>

                        <p id="unifiedSidebarInfo">
                            Loading...
                        </p>

                        <span class="placement-ready-badge">
                            ● Placement Ready
                        </span>

                    </div>

                    <div class="student-profile-progress">

                        <div class="student-profile-progress-head">

                            <span>
                                Profile
                            </span>

                            <strong id="sidebarProfilePercent">
                                0%
                            </strong>

                        </div>

                        <div class="student-profile-progress-track">

                            <div
                                id="sidebarProfileProgressBar"
                                class="student-profile-progress-bar"
                            ></div>

                        </div>

                    </div>

                </div>

                <p class="student-unified-title">
                    MAIN MENU
                </p>

                <nav class="student-unified-menu">

                    ${menuHtml}

                </nav>

                <div class="student-sidebar-divider"></div>

                <a
                    class="student-unified-menu-item ${notificationActive}"
                    href="student-notifications.html"
                >
                    <span class="student-menu-icon">
                        ●
                    </span>

                    <span class="student-menu-label">
                        Notifications
                    </span>

                    <span
                        id="sidebarNotificationCount"
                        class="student-sidebar-badge notification-badge"
                        style="display:none;"
                    >
                        0
                    </span>
                </a>

                <a
                    class="student-unified-menu-item"
                    href="student-help.html"
                >
                    <span class="student-menu-icon">
                        ?
                    </span>

                    <span class="student-menu-label">
                        Help & Support
                    </span>
                </a>

                <div class="student-sidebar-divider"></div>

                <a
                    class="student-unified-menu-item ${settingsActive}"
                    href="student-settings.html"
                >
                    <span class="student-menu-icon">
                        ⚙
                    </span>

                    <span class="student-menu-label">
                        Settings
                    </span>
                </a>

                <a
                    class="student-unified-menu-item"
                    href="#"
                    onclick="unifiedStudentLogout(event)"
                >
                    <span class="student-menu-icon">
                        ↪
                    </span>

                    <span class="student-menu-label">
                        Sign Out
                    </span>
                </a>

                <div class="student-unified-help">

                    <div class="student-help-logo">
                        ◆
                    </div>

                    <div>
                        <h4>
                            Same Campus.
                        </h4>

                        <p>
                            Bigger Opportunities.
                        </p>
                    </div>

                    <span class="student-help-arrow">
                        →
                    </span>

                </div>

            </aside>
        `;
    }

    function replaceOldSidebar() {

        const oldSidebar = document.querySelector(
            "aside.sidebar, " +
            "aside.profile-sidebar, " +
            "aside.student-settings-sidebar, " +
            "aside.student-unified-sidebar"
        );

        if (oldSidebar) {
            oldSidebar.outerHTML = createSidebarHtml();
            return;
        }

        document.body.insertAdjacentHTML(
            "afterbegin",
            createSidebarHtml()
        );
    }

    function calculateProfileCompletion(student) {

        const fields = [
            student.name,
            student.email,
            student.mobile,
            student.rollNumber,
            student.branch,
            student.year,
            student.cgpa,
            student.skills,
            student.address,
            student.linkedin,
            student.github,
            student.resumeFileName,
            student.photoFileName
        ];

        const completed = fields.filter(function (value) {
            return value !== null &&
                value !== undefined &&
                String(value).trim() !== "";
        }).length;

        return Math.round(
            (completed / fields.length) * 100
        );
    }

    async function loadStudentSidebarData() {

        const photo =
            document.getElementById(
                "unifiedSidebarPhoto"
            );

        const name =
            document.getElementById(
                "unifiedSidebarName"
            );

        const info =
            document.getElementById(
                "unifiedSidebarInfo"
            );

        if (!studentId) {

            if (name) {
                name.innerText = "Guest Student";
            }

            if (info) {
                info.innerText = "Please login";
            }

            return;
        }

        try {

            const response = await fetch(
                "/api/students/" + studentId
            );

            if (!response.ok) {
                throw new Error("Student not found");
            }

            const student =
                await response.json();

            if (name) {
                name.innerText =
                    student.name || "Student";
            }

            if (info) {

                const studentInfo = [
                    student.branch || "Student",
                    student.year
                        ? student.year + " Year"
                        : ""
                ]
                    .filter(Boolean)
                    .join(" • ");

                info.innerText =
                    studentInfo ||
                    "RecruitEase Student";
            }

            if (photo) {

                photo.onerror = function () {
                    photo.src =
                        defaultAvatar();
                };

                photo.src =
                    "/api/students/" +
                    studentId +
                    "/photo?t=" +
                    Date.now();
            }

            const percentage =
                calculateProfileCompletion(
                    student
                );

            const percentageText =
                document.getElementById(
                    "sidebarProfilePercent"
                );

            const progressBar =
                document.getElementById(
                    "sidebarProfileProgressBar"
                );

            if (percentageText) {
                percentageText.innerText =
                    percentage + "%";
            }

            if (progressBar) {
                progressBar.style.width =
                    percentage + "%";
            }

        } catch (error) {

            console.error(
                "Student sidebar load error:",
                error
            );

            if (photo) {
                photo.src =
                    defaultAvatar();
            }

            if (info) {
                info.innerText =
                    "RecruitEase Student";
            }
        }
    }

    window.unifiedStudentLogout =
        function (event) {

            if (event) {
                event.preventDefault();
            }

            localStorage.removeItem(
                "studentId"
            );

            window.location.href =
                "student-login.html";
        };

    document.addEventListener(
        "DOMContentLoaded",
        function () {

            const params =
                new URLSearchParams(
                    window.location.search
                );

            const isAdminView =
                params.get("adminView") ===
                "true";

            if (isAdminView) {
                return;
            }

            loadSidebarCss();

            replaceOldSidebar();

            loadStudentSidebarData();
        }
    );

})();