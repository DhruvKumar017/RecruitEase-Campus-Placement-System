(function () {
    const THEME_KEY = "recruiteaseAdminTheme";
    const PHOTO_KEY = "recruiteaseAdminPhoto";
    const PROFILE_KEY = "recruiteaseAdminProfile";

    function getAdminProfile() {
        try {
            return JSON.parse(localStorage.getItem(PROFILE_KEY) || "{}");
        } catch (error) {
            return {};
        }
    }

    function loadAdminPhotoAndName() {
        const profile = getAdminProfile();
        const savedPhoto = localStorage.getItem(PHOTO_KEY);
        const adminName = profile.name || "Admin";

        document.querySelectorAll(
            ".admin-profile img, #topAdminPhoto, #sharedAdminPhoto, #adminPhoto"
        ).forEach(function (image) {
            if (savedPhoto) {
                image.src = savedPhoto;
            }

            image.onerror = function () {
                this.src = "https://i.pravatar.cc/80?img=12";
            };
        });

        document.querySelectorAll(
            ".admin-profile b, #topAdminName, #sharedAdminName"
        ).forEach(function (element) {
            element.innerText = adminName;
        });
    }

    function applyTheme(theme) {
        const isLight = theme === "light";

        document.body.classList.toggle("recruitease-light-mode", isLight);

        /*
         Different pages used old theme class names.
         Remove them so only one theme system works.
        */
        document.body.classList.remove("dark-mode");
        document.body.classList.remove("light-mode");

        document.querySelectorAll(".theme-btn, #themeToggle").forEach(function (button) {
            button.innerText = isLight ? "☀️" : "🌙";
        });
    }

    function closeToolbarPopup() {
        document.querySelectorAll(".recruitease-toolbar-popup").forEach(function (popup) {
            popup.remove();
        });
    }

    function logoutAdmin() {
        localStorage.removeItem("adminUsername");
        localStorage.removeItem("adminRole");
        localStorage.removeItem(PHOTO_KEY);
        localStorage.removeItem(PROFILE_KEY);

        window.location.href = "index.html";
    }

    function showProfileMenu(profileBox) {
        closeToolbarPopup();

        const rect = profileBox.getBoundingClientRect();

        const popup = document.createElement("div");
        popup.className = "recruitease-toolbar-popup profile-popup";

        popup.style.top = (rect.bottom + 10) + "px";
        popup.style.left = Math.max(12, rect.right - 220) + "px";

        popup.innerHTML = `
            <button type="button" id="toolbarSettingsBtn">⚙️ Settings</button>
            <button type="button" id="toolbarLogoutBtn">↪ Logout</button>
        `;

        document.body.appendChild(popup);

        document.getElementById("toolbarSettingsBtn").onclick = function () {
            window.location.href = "settings.html";
        };

        document.getElementById("toolbarLogoutBtn").onclick = function () {
            logoutAdmin();
        };
    }

    function addToolbarStyle() {
        const oldStyle = document.getElementById("recruiteaseToolbarStyle");

        if (oldStyle) {
            oldStyle.remove();
        }

        const style = document.createElement("style");
        style.id = "recruiteaseToolbarStyle";

        style.innerHTML = `
            /* ================= PROFILE POPUP ================= */

            .recruitease-toolbar-popup{
                position:fixed;
                width:210px;
                z-index:99999;
                padding:8px;
                border-radius:14px;
                background:#081528;
                border:1px solid rgba(47,128,255,.35);
                box-shadow:0 18px 45px rgba(0,0,0,.38);
                color:#ffffff;
            }

            .profile-popup button{
                width:100%;
                padding:10px;
                border:none;
                border-radius:9px;
                background:transparent;
                color:#e7f0ff;
                text-align:left;
                font-size:13px;
                cursor:pointer;
            }

            .profile-popup button:hover{
                background:rgba(47,128,255,.18);
            }

            /* ================= GLOBAL LIGHT MODE ================= */

            body.recruitease-light-mode{
                background:#eef4ff !important;
                color:#0f172a !important;
            }

            body.recruitease-light-mode .main-content{
                background:
                    radial-gradient(
                        circle at 60% 0%,
                        rgba(47,128,255,.14),
                        transparent 35%
                    ),
                    #eef4ff !important;
                color:#0f172a !important;
            }

            /* SIDEBAR — Dashboard jaisa light blue */

            body.recruitease-light-mode .sidebar{
                background:linear-gradient(180deg,#e2efff 0%,#c8ddff 100%) !important;
                border-right:1px solid #adcaf6 !important;
            }

            body.recruitease-light-mode .brand{
                border-bottom-color:#aecaf2 !important;
            }

            body.recruitease-light-mode .brand h2{
                color:#0f172a !important;
            }

            body.recruitease-light-mode .brand p{
                color:#52647f !important;
            }

            body.recruitease-light-mode .sidebar .menu-title{
                color:#53647f !important;
            }

            body.recruitease-light-mode .sidebar a{
                color:#172640 !important;
            }

            body.recruitease-light-mode .sidebar a:hover,
            body.recruitease-light-mode .sidebar a.active{
                background:linear-gradient(135deg,#1e40af,#3b82f6) !important;
                color:#ffffff !important;
                box-shadow:0 10px 25px rgba(37,99,235,.28) !important;
            }

            /* HEADINGS + NORMAL TEXT */

            body.recruitease-light-mode .main-content h1,
            body.recruitease-light-mode .main-content h2,
            body.recruitease-light-mode .main-content h3,
            body.recruitease-light-mode .main-content h4,
            body.recruitease-light-mode .main-content h5,
            body.recruitease-light-mode .main-content h6,
            body.recruitease-light-mode .main-content th,
            body.recruitease-light-mode .main-content strong,
            body.recruitease-light-mode .main-content .student-name,
            body.recruitease-light-mode .main-content .admin-profile b{
                color:#0f172a !important;
            }

            body.recruitease-light-mode .main-content p,
            body.recruitease-light-mode .main-content small,
            body.recruitease-light-mode .main-content label,
            body.recruitease-light-mode .main-content td,
            body.recruitease-light-mode .main-content .reason,
            body.recruitease-light-mode .main-content .legend,
            body.recruitease-light-mode .main-content .breadcrumb,
            body.recruitease-light-mode .main-content .table-footer p{
                color:#5e708b !important;
            }

            /* WHITE CARDS / PANELS */

            body.recruitease-light-mode .stat-card,
            body.recruitease-light-mode .panel,
            body.recruitease-light-mode .table-panel,
            body.recruitease-light-mode .analytics-card,
            body.recruitease-light-mode .placed-panel,
            body.recruitease-light-mode .settings-card,
            body.recruitease-light-mode .help-panel,
            body.recruitease-light-mode .side-card,
            body.recruitease-light-mode .bottom-card,
            body.recruitease-light-mode .content-card,
            body.recruitease-light-mode .data-card,
            body.recruitease-light-mode .list-card,
            body.recruitease-light-mode .report-card,
            body.recruitease-light-mode .status-panel,
            body.recruitease-light-mode .interview-card,
            body.recruitease-light-mode .test-card,
            body.recruitease-light-mode .upcoming-card,
            body.recruitease-light-mode .quick-actions,
            body.recruitease-light-mode [class$="-panel"],
            body.recruitease-light-mode [class*="-panel "]{
                background:#ffffff !important;
                border-color:#cfe0fb !important;
                color:#0f172a !important;
                box-shadow:0 10px 28px rgba(39,84,145,.06) !important;
            }

            /* TOP SEARCH, PROFILE, BELL */

            body.recruitease-light-mode .top-actions input,
            body.recruitease-light-mode .search-box,
            body.recruitease-light-mode .filter-search,
            body.recruitease-light-mode .filter-select,
            body.recruitease-light-mode input,
            body.recruitease-light-mode select,
            body.recruitease-light-mode textarea,
            body.recruitease-light-mode .admin-profile,
            body.recruitease-light-mode .bell,
            body.recruitease-light-mode .theme-btn,
            body.recruitease-light-mode .notification-dropdown{
                background:#ffffff !important;
                border-color:#cfe0fb !important;
                color:#0f172a !important;
            }

            body.recruitease-light-mode input::placeholder,
            body.recruitease-light-mode textarea::placeholder{
                color:#8190a8 !important;
            }

            body.recruitease-light-mode select option{
                background:#ffffff !important;
                color:#0f172a !important;
            }

            /* TABLE / FILTER AREA */

            body.recruitease-light-mode .filter-bar,
            body.recruitease-light-mode .filters,
            body.recruitease-light-mode .filter-section{
                background:#f8fbff !important;
                border-color:#dce8fb !important;
            }

            body.recruitease-light-mode .table-wrapper,
            body.recruitease-light-mode .table-container,
            body.recruitease-light-mode table,
            body.recruitease-light-mode tbody{
                background:#ffffff !important;
            }

            body.recruitease-light-mode table thead th,
            body.recruitease-light-mode .deleted-table th{
                background:#eaf2ff !important;
                color:#18335f !important;
                border-bottom:1px solid #d6e5fb !important;
            }

            body.recruitease-light-mode table tbody tr,
            body.recruitease-light-mode .deleted-table tr{
                background:#ffffff !important;
            }

            body.recruitease-light-mode table tbody tr:hover,
            body.recruitease-light-mode .deleted-table tr:hover{
                background:#f4f8ff !important;
            }

            body.recruitease-light-mode table td,
            body.recruitease-light-mode .deleted-table td{
                border-bottom-color:#e2ebf8 !important;
                color:#50627d !important;
            }

            /* CHART / INFO AREA */

            body.recruitease-light-mode .line-chart,
            body.recruitease-light-mode .chart-box,
            body.recruitease-light-mode .chart-container{
                background:#f8fbff !important;
                border-color:#dfe9f7 !important;
            }

            body.recruitease-light-mode .info-card{
                background:linear-gradient(90deg,#e6f0ff,#d8e9ff) !important;
                border-color:#bcd7ff !important;
            }

            body.recruitease-light-mode .info-card p{
                color:#36577f !important;
            }

            /* NORMAL SECONDARY BUTTONS */

            body.recruitease-light-mode .clear-filter-btn,
            body.recruitease-light-mode .pagination button,
            body.recruitease-light-mode .panel-head button,
            body.recruitease-light-mode .quick-actions button,
            body.recruitease-light-mode .table-footer button{
                background:#f4f8ff !important;
                color:#2563eb !important;
                border:1px solid #cbdffd !important;
            }

            body.recruitease-light-mode .pagination button.active{
                background:#2563eb !important;
                color:#ffffff !important;
            }

            /* MAIN BLUE ACTION BUTTONS */

            body.recruitease-light-mode .report-btn,
            body.recruitease-light-mode .add-btn,
            body.recruitease-light-mode .create-btn,
            body.recruitease-light-mode .save-btn,
            body.recruitease-light-mode .submit-btn,
            body.recruitease-light-mode .schedule-btn,
            body.recruitease-light-mode .primary-btn,
            body.recruitease-light-mode button[class*="add"],
            body.recruitease-light-mode button[class*="create"],
            body.recruitease-light-mode button[class*="save"],
            body.recruitease-light-mode button[class*="submit"],
            body.recruitease-light-mode button[class*="schedule"],
            body.recruitease-light-mode button[class*="export"],
            body.recruitease-light-mode button[class*="report"]{
                background:linear-gradient(135deg,#1e40af,#2563eb) !important;
                color:#ffffff !important;
                border-color:#2563eb !important;
            }

            /* RESTORE + DELETE BUTTONS */

            body.recruitease-light-mode .restore-btn{
                background:rgba(22,199,132,.14) !important;
                color:#0e9f63 !important;
                border:1px solid rgba(22,199,132,.25) !important;
            }

            body.recruitease-light-mode .delete-btn{
                background:rgba(255,59,92,.12) !important;
                color:#e11d48 !important;
                border:1px solid rgba(255,59,92,.20) !important;
            }

            /* PROFILE POPUP LIGHT MODE */

            body.recruitease-light-mode .recruitease-toolbar-popup{
                background:#ffffff !important;
                color:#0f172a !important;
                border-color:#cfe0fb !important;
                box-shadow:0 18px 45px rgba(15,23,42,.16) !important;
            }

            body.recruitease-light-mode .profile-popup button{
                color:#0f172a !important;
            }

            body.recruitease-light-mode .profile-popup button:hover{
                background:#edf4ff !important;
            }

            body.recruitease-light-mode ::-webkit-scrollbar-track{
                background:#eef4ff !important;
            }
        `;

        document.head.appendChild(style);
    }

    document.addEventListener("DOMContentLoaded", function () {
        addToolbarStyle();
        loadAdminPhotoAndName();

        const savedTheme = localStorage.getItem(THEME_KEY) || "dark";
        applyTheme(savedTheme);
    });

    document.addEventListener("click", function (event) {
        const themeButton = event.target.closest(".theme-btn, #themeToggle");

        if (themeButton) {
            event.preventDefault();
            event.stopPropagation();

            const nextTheme =
                document.body.classList.contains("recruitease-light-mode")
                    ? "dark"
                    : "light";

            localStorage.setItem(THEME_KEY, nextTheme);
            applyTheme(nextTheme);
            closeToolbarPopup();
            return;
        }

        /*
         Bell click handle nahi ki gayi.
         Har page ka existing bell / notification code kaam karega.
        */

        const profileBox = event.target.closest(".admin-profile");

        if (profileBox) {
            event.preventDefault();
            event.stopPropagation();
            showProfileMenu(profileBox);
            return;
        }

        if (!event.target.closest(".recruitease-toolbar-popup")) {
            closeToolbarPopup();
        }
    }, true);

    window.addEventListener("storage", function () {
        loadAdminPhotoAndName();
        applyTheme(localStorage.getItem(THEME_KEY) || "dark");
    });
})();