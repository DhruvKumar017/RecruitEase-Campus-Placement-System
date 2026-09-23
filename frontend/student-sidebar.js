(function () {
    const studentId = localStorage.getItem("studentId");

    const menuItems = [
        {
            file: "student-dashboard.html",
            icon: "🏠",
            label: "Dashboard"
        },
        {
            file: "student-profile.html",
            icon: "👤",
            label: "My Profile"
        },
        {
            file: "student-eligible-companies.html",
            icon: "🏢",
            label: "Eligible Companies"
        },
        {
            file: "student-placement-drives.html",
            icon: "💼",
            label: "Placement Drives"
        },
        {
            file: "student-applications.html",
            icon: "📄",
            label: "My Applications"
        },
        {
            file: "student-aptitude-tests.html",
            icon: "🧠",
            label: "Aptitude Tests"
        },
        {
            file: "student-interviews.html",
            icon: "🎤",
            label: "Interviews"
        },
        {
            file: "student-resume.html",
            icon: "📑",
            label: "Resume Builder"
        },
        {
            file: "student-notifications.html",
            icon: "🔔",
            label: "Notifications"
        }
    ];

    function getCurrentFileName() {
        const path = window.location.pathname;
        const fileName = path.substring(path.lastIndexOf("/") + 1);

        return fileName || "student-dashboard.html";
    }

    function defaultAvatar() {
        return "data:image/svg+xml;charset=UTF-8," + encodeURIComponent(`
            <svg xmlns="http://www.w3.org/2000/svg" width="220" height="220">
                <rect width="100%" height="100%" fill="#1d4ed8"/>
                <text x="50%" y="53%"
                      dominant-baseline="middle"
                      text-anchor="middle"
                      font-family="Arial"
                      font-size="70"
                      font-weight="bold"
                      fill="white">
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
                currentFile === item.file ? "active" : "";

            return `
                <a class="${activeClass}" href="${item.file}">
                    ${item.icon} ${item.label}
                </a>
            `;
        }).join("");

        const settingsActive =
            currentFile === "student-settings.html"
                ? "active"
                : "";

        return `
            <aside class="student-unified-sidebar">

                <div class="student-unified-brand">
                    <div class="student-unified-brand-icon">🎓</div>

                    <div>
                        <h2>Recruit<span>Ease</span></h2>
                        <p>Student Portal</p>
                    </div>
                </div>

                <div class="student-unified-user">
                    <img
                        id="unifiedSidebarPhoto"
                        src="${defaultAvatar()}"
                        alt="Student Photo"
                    >

                    <div>
                        <b id="unifiedSidebarName">Student</b>
                        <p id="unifiedSidebarInfo">Loading...</p>
                    </div>
                </div>

                <p class="student-unified-title">MAIN MENU</p>

                ${menuHtml}

                <p class="student-unified-title">ACCOUNT</p>

                <a class="${settingsActive}" href="student-settings.html">
                    ⚙ Settings
                </a>

                <a href="#" onclick="unifiedStudentLogout(event)">
                    ↪ Logout
                </a>

                <div class="student-unified-help">
                    <h4>🎧 Need Help?</h4>
                    <p>Contact your TPO for placement support.</p>
                </div>

            </aside>
        `;
    }

    function replaceOldSidebar() {
        const oldSidebar = document.querySelector(
            "aside.sidebar, aside.profile-sidebar, aside.student-settings-sidebar"
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

    async function loadStudentSidebarData() {
        const photo = document.getElementById("unifiedSidebarPhoto");
        const name = document.getElementById("unifiedSidebarName");
        const info = document.getElementById("unifiedSidebarInfo");

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
            const response = await fetch("/api/students/" + studentId);

            if (!response.ok) {
                throw new Error("Student not found");
            }

            const student = await response.json();

            if (name) {
                name.innerText = student.name || "Student";
            }

            if (info) {
                const studentInfo = [
                    student.branch || "Student",
                    student.year || ""
                ]
                    .filter(Boolean)
                    .join(" · ");

                info.innerText =
                    studentInfo || "RecruitEase Student";
            }

            if (photo) {
                photo.onerror = function () {
                    photo.src = defaultAvatar();
                };

                photo.src =
                    "/api/students/" +
                    studentId +
                    "/photo?t=" +
                    Date.now();
            }

        } catch (error) {
            console.error("Student sidebar load error:", error);

            if (photo) {
                photo.src = defaultAvatar();
            }

            if (info) {
                info.innerText = "RecruitEase Student";
            }
        }
    }

    window.unifiedStudentLogout = function (event) {
        if (event) {
            event.preventDefault();
        }

        localStorage.removeItem("studentId");

        window.location.href = "student-login.html";
    };

    document.addEventListener("DOMContentLoaded", function () {
        const params = new URLSearchParams(window.location.search);

        const isAdminView =
            params.get("adminView") === "true";

        /* Admin View me student sidebar kabhi nahi lagegi */
        if (isAdminView) {
            return;
        }

        loadSidebarCss();
        replaceOldSidebar();
        loadStudentSidebarData();
    });
})();