package com.recruitease.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.security.enforce-jwt:false}")
    private boolean enforceJwt;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)

            .sessionManagement(session -> session
                .sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .formLogin(AbstractHttpConfigurer::disable)

            .httpBasic(AbstractHttpConfigurer::disable)

            .authorizeHttpRequests(authorize -> {


/*
 * Viewer credential generation requires MAIN_ADMIN.
 * Viewer login remains public so credentials can be redeemed.
 *
 * Existing application compatibility is preserved.
 */
if (!enforceJwt) {
    authorize

        // Demo Viewer APIs require a valid DEMO_VIEWER JWT.
        .requestMatchers(
            "/api/demo-viewer/**"
        )
        .hasRole("DEMO_VIEWER")

        .requestMatchers(
            "/api/admin-viewer/generate"
        )
        .hasRole("MAIN_ADMIN")
.requestMatchers(
    HttpMethod.POST,
    "/api/admin-viewer/otp/send",
    "/api/admin-viewer/otp/verify"
)
.permitAll()

.requestMatchers(
    "/api/admin-viewer/login"
)
.permitAll()
// Only Admin and Main Admin can view login history
.requestMatchers(
    HttpMethod.GET,
    "/api/admin-viewer/history"
)
.hasAnyRole("MAIN_ADMIN", "ADMIN")

.requestMatchers(
    HttpMethod.GET,
    "/api/admin-viewer/dashboard"
)
.hasRole("VIEWER")

.requestMatchers(
    "/api/admin-viewer/**"
)
.denyAll()

// Prevent unauthenticated login-record creation.
.requestMatchers(
    HttpMethod.POST,
    "/api/student-login/create"
)
.denyAll()

// Student management modifications:
// Only authenticated Admin or Main Admin.
.requestMatchers(
    HttpMethod.PUT,
    "/api/students/*/block",
    "/api/students/*/unblock",
    "/api/students/*/delete",
    "/api/students/*/restore",
    "/api/students/*/update"
)
.hasAnyRole("MAIN_ADMIN", "ADMIN")

.requestMatchers(
    HttpMethod.DELETE,
    "/api/students/*/permanent"
)
.hasAnyRole("MAIN_ADMIN", "ADMIN")

// Student profile update and uploads:
// Require an authenticated Admin or Main Admin.
.requestMatchers(
    HttpMethod.PUT,
    "/api/students/*/update-profile"
)
.access((authentication, context) -> {
    Authentication auth = authentication.get();

    boolean isAdmin = auth.getAuthorities().stream()
        .anyMatch(a ->
            a.getAuthority().equals("ROLE_MAIN_ADMIN") ||
            a.getAuthority().equals("ROLE_ADMIN")
        );

    if (isAdmin) {
        return new org.springframework.security.authorization
            .AuthorizationDecision(true);
    }

    boolean isStudent = auth.getAuthorities().stream()
        .anyMatch(a ->
            a.getAuthority().equals("ROLE_STUDENT")
        );

    String path = context.getRequest().getServletPath();

    boolean isOwner = false;

    if (isStudent) {
        String[] parts = path.split("/");

        if (parts.length == 5) {
            isOwner = parts[3].equals(auth.getName());
        }
    }

    return new org.springframework.security.authorization
        .AuthorizationDecision(isOwner);
})

.requestMatchers(
    HttpMethod.POST,
    "/api/students/*/upload-resume",
    "/api/students/*/upload-photo"
)
.access((authentication, context) -> {
    Authentication auth = authentication.get();

    boolean isAdmin = auth.getAuthorities().stream()
        .anyMatch(a ->
            a.getAuthority().equals("ROLE_MAIN_ADMIN") ||
            a.getAuthority().equals("ROLE_ADMIN")
        );

    if (isAdmin) {
        return new org.springframework.security.authorization
            .AuthorizationDecision(true);
    }

    boolean isStudent = auth.getAuthorities().stream()
        .anyMatch(a ->
            a.getAuthority().equals("ROLE_STUDENT")
        );

    String path = context.getRequest().getServletPath();
    String[] parts = path.split("/");

    boolean isOwner = isStudent
        && parts.length == 5
        && parts[3].equals(auth.getName());

    return new org.springframework.security.authorization
        .AuthorizationDecision(isOwner);
})

// Protect Admin Settings and Notifications APIs.
.requestMatchers(
    "/api/admin-settings",
    "/api/admin-settings/**",
    "/api/admin-notifications",
    "/api/admin-notifications/**"
)
.hasAnyRole("MAIN_ADMIN", "ADMIN")

// Protect real student list from public access.
.requestMatchers(
    HttpMethod.GET,
    "/api/students"
)
.hasAnyRole("MAIN_ADMIN", "ADMIN")

// Only logged-in students can view their own complaints.
.requestMatchers(
    HttpMethod.GET,
    "/api/student-issues/my"
)
.hasRole("STUDENT")

// Only Admin / Main Admin can reply to student issues.
.requestMatchers(
    HttpMethod.PUT,
    "/api/student-issues/*/reply"
)
.hasAnyRole("MAIN_ADMIN", "ADMIN")

// Only Admin / Main Admin can update issue status.
.requestMatchers(
    HttpMethod.PUT,
    "/api/student-issues/*/status/*"
)
.hasAnyRole("MAIN_ADMIN", "ADMIN")

// Only Admin / Main Admin can view all student issues.
.requestMatchers(
    HttpMethod.GET,
    "/api/student-issues",
    "/api/student-issues/pending"
)
.hasAnyRole("MAIN_ADMIN", "ADMIN")

.anyRequest()
.permitAll();

    return;
}


                /*
                 * Later strict mode me ye rules active honge.
                 */
                authorize
                    .requestMatchers(
                        "/",
                        "/index.html",
                        "/*.html",
                        "/*.css",
                        "/*.js",
                        "/uploads/**",
"/api/admin-login/**",
"/api/student-login/**",
"/api/admin-viewer/login",
"/api/admin-viewer/otp/send",
"/api/admin-viewer/otp/verify",
"/api/students/register"
)
.permitAll()

// Students can submit complaints without logging in.
// Verified complaint history also accepts credentials.
.requestMatchers(
    HttpMethod.POST,
    "/api/student-issues",
    "/api/student-issues/verify-history"
)
.permitAll()

                   // Only logged-in students can view their complaints.
.requestMatchers(
    HttpMethod.GET,
    "/api/student-issues/my"
)
.hasRole("STUDENT")

// Only Admin / Main Admin can reply to complaints.
.requestMatchers(
    HttpMethod.PUT,
    "/api/student-issues/*/reply"
)
.hasAnyRole("MAIN_ADMIN", "ADMIN")

// Only Admin / Main Admin can update complaint status.
.requestMatchers(
    HttpMethod.PUT,
    "/api/student-issues/*/status/*"
)
.hasAnyRole("MAIN_ADMIN", "ADMIN")

// Only Admin / Main Admin can view all complaints.
.requestMatchers(
    HttpMethod.GET,
    "/api/student-issues",
    "/api/student-issues/pending"
)
.hasAnyRole("MAIN_ADMIN", "ADMIN")

.requestMatchers(
    "/api/admin-settings/**",
    "/api/admin-notifications/**"
)
.hasAnyRole("MAIN_ADMIN", "ADMIN")
// Secure viewer login history
.requestMatchers(
    HttpMethod.GET,
    "/api/admin-viewer/history"
)
.hasAnyRole("MAIN_ADMIN", "ADMIN")
.requestMatchers("/api/demo-viewer/**")
.hasRole("DEMO_VIEWER")

.requestMatchers(
    HttpMethod.GET,
    "/api/admin-viewer/dashboard"
)
.hasRole("VIEWER")

.requestMatchers("/api/admin-viewer/**")
.denyAll()

.requestMatchers("/api/**")
.authenticated()

                    .anyRequest()
                    .permitAll();
            })


.addFilterBefore(
    jwtAuthenticationFilter,
    UsernamePasswordAuthenticationFilter.class
)

.addFilterAfter(
    new OncePerRequestFilter() {

        @Override
        protected void doFilterInternal(
                jakarta.servlet.http.HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response,
                jakarta.servlet.FilterChain filterChain
        ) throws jakarta.servlet.ServletException, java.io.IOException {

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

boolean isViewer =
        authentication != null &&
        authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_VIEWER".equals(authority.getAuthority())
                        || "ROLE_DEMO_VIEWER".equals(authority.getAuthority())
                );
// Viewer can access only explicitly approved APIs.
if (isViewer) {

    String path = request.getServletPath();

boolean isDemoViewer =
        authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_DEMO_VIEWER".equals(
                                authority.getAuthority()
                        )
                );

boolean allowedViewerApi =
        HttpMethod.GET.matches(request.getMethod())
        && (
            (isDemoViewer
                && path.equals("/api/demo-viewer/dashboard"))
            || (!isDemoViewer
                && path.equals("/api/admin-viewer/dashboard"))
        );

    if (!allowedViewerApi) {
        response.sendError(
                403,
                "Viewer is not authorized to access this resource."
        );
        return;
    }
}

            String method = request.getMethod();

            boolean isWriteRequest =
                    HttpMethod.POST.matches(method) ||
                    HttpMethod.PUT.matches(method) ||
                    HttpMethod.PATCH.matches(method) ||
                    HttpMethod.DELETE.matches(method);

            if (isViewer && isWriteRequest) {
                response.sendError(
                        403,
                        "Viewer account has read-only access."
                );
                return;
            }

            filterChain.doFilter(request, response);
        }
    },
    JwtAuthenticationFilter.class
);


        return http.build();
    }
}