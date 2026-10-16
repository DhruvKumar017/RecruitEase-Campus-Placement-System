
package com.recruitease.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo-viewer")
public class DemoViewerController {

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {

        return ResponseEntity.ok(
            Map.of(
                "demoMode", true,
                "portalName", "RecruitEase Demo Admin Portal",

                "totalStudents", 1250,
                "totalCompanies", 45,
                "totalDrives", 28,
                "placedStudents", 680,

                "companies", List.of(
                    Map.of(
                        "name", "Demo Tech Solutions",
                        "role", "Software Engineer",
                        "status", "Active"
                    ),
                    Map.of(
                        "name", "Sample IT Services",
                        "role", "Java Developer",
                        "status", "Upcoming"
                    ),
                    Map.of(
                        "name", "Example Innovations",
                        "role", "Data Analyst",
                        "status", "Completed"
                    )
                ),

                "message", "This dashboard contains sample data only."
            )
        );
    }
}
