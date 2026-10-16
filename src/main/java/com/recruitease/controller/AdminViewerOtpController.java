
package com.recruitease.controller;

import com.recruitease.service.AdminViewerOtpService;
import com.recruitease.security.JwtService;
import com.recruitease.service.AdminViewerService;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin-viewer/otp")
public class AdminViewerOtpController {

private final AdminViewerOtpService otpService;
private final JwtService jwtService;
private final AdminViewerService viewerService;

public AdminViewerOtpController(
        AdminViewerOtpService otpService,
        JwtService jwtService,
        AdminViewerService viewerService
) {
    this.otpService = otpService;
    this.jwtService = jwtService;
    this.viewerService = viewerService;
}

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(
            @RequestBody Map<String, String> request
    ) {
        try {
            otpService.sendOtp(request.get("email"));

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "OTP sent successfully."
                    )
            );

        } catch (IllegalArgumentException exception) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", exception.getMessage()
                    )
            );

        } catch (IllegalStateException exception) {

            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of(
                            "success", false,
                            "message", exception.getMessage()
                    )
            );

        } catch (Exception exception) {

            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR
            ).body(
                    Map.of(
                            "success", false,
                            "message", "Unable to send OTP."
                    )
            );
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(
            @RequestBody Map<String, String> request
    ) {
        try {
Map<String, String> credentials =
        otpService.verifyAndGenerateCredentials(
                request.get("email"),
                request.get("otp"),
                viewerService
        );

if (credentials == null) {
    return ResponseEntity.badRequest().body(
            Map.of(
                    "success", false,
                    "message", "Invalid or expired OTP."
            )
    );
}

return ResponseEntity.ok(
        Map.of(
                "success", true,
                "message", "Temporary Demo Viewer credentials generated successfully.",
                "viewerId", credentials.get("viewerId"),
                "password", credentials.get("password"),
                "expiresIn", credentials.get("expiresIn")
        )
);
} catch (IllegalArgumentException exception) {

    return ResponseEntity.badRequest().body(
            Map.of(
                    "success", false,
                    "message", exception.getMessage()
            )
    );

} catch (IllegalStateException exception) {

    return ResponseEntity.status(
            HttpStatus.TOO_MANY_REQUESTS
    ).body(
            Map.of(
                    "success", false,
                    "message", exception.getMessage()
            )
    );

} catch (Exception exception) {

            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR
            ).body(
                    Map.of(
                            "success", false,
                            "message", "OTP verification failed."
                    )
            );
        }
    }
}
