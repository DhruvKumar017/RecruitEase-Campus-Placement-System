(function () {
    const originalFetch = window.fetch.bind(window);

    function getRequestUrl(input) {
        if (typeof input === "string") {
            return new URL(input, window.location.origin);
        }

        if (input instanceof Request) {
            return new URL(input.url, window.location.origin);
        }

        return new URL(String(input), window.location.origin);
    }

    function isSameOriginApiRequest(input) {
        try {
            const url = getRequestUrl(input);

            return (
                url.origin === window.location.origin &&
                url.pathname.startsWith("/api/")
            );
        } catch (error) {
            return false;
        }
    }

    window.fetch = async function (input, init = {}) {
        if (!isSameOriginApiRequest(input)) {
            return originalFetch(input, init);
        }

        const token = localStorage.getItem("adminToken");

        const headers = new Headers(
            init.headers ||
            (input instanceof Request ? input.headers : undefined)
        );

        if (token) {
            headers.set(
                "Authorization",
                "Bearer " + token
            );
        }

        return originalFetch(input, {
            ...init,
            headers: headers
        });
    };

    window.adminLogout = function () {
        localStorage.removeItem("adminUsername");
        localStorage.removeItem("adminRole");
        localStorage.removeItem("adminToken");

        window.location.href = "admin-login.html";
    };
})();