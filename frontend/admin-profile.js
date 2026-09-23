(function () {
    const PHOTO_KEY = "recruiteaseAdminPhoto";
    const PROFILE_KEY = "recruiteaseAdminProfile";
    const DEFAULT_PHOTO = "https://i.pravatar.cc/80?img=12";

    function getAdminProfile() {
        try {
            return JSON.parse(localStorage.getItem(PROFILE_KEY) || "{}");
        } catch (error) {
            return {};
        }
    }

    function loadSharedAdminProfile() {
        const savedPhoto = localStorage.getItem(PHOTO_KEY);
        const profile = getAdminProfile();

        const adminName = profile.name || "Admin";
        const photoToShow = savedPhoto || DEFAULT_PHOTO;

        document.querySelectorAll(
            ".admin-profile img, .student-admin-profile img, #topAdminPhoto, #sharedAdminPhoto, #adminPhoto, #adminProfilePhoto"
        ).forEach(function (image) {
            image.src = photoToShow;

            image.onerror = function () {
                this.src = DEFAULT_PHOTO;
            };
        });

        document.querySelectorAll(
            ".admin-profile b, .student-admin-profile b, #topAdminName, #sharedAdminName, #adminDisplayName, #profileDisplayName"
        ).forEach(function (nameElement) {
            nameElement.innerText = adminName;
        });
    }

    document.addEventListener("DOMContentLoaded", loadSharedAdminProfile);

    window.addEventListener("storage", function (event) {
        if (
            event.key === PHOTO_KEY ||
            event.key === PROFILE_KEY
        ) {
            loadSharedAdminProfile();
        }
    });

    window.addEventListener(
        "recruiteaseAdminProfileChanged",
        loadSharedAdminProfile
    );
})();