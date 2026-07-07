package com.recruitease.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.recruitease.entity.AptitudeTest;
import com.recruitease.entity.TestQuestion;
import com.recruitease.repository.ApplicationRepository;
import com.recruitease.repository.AptitudeTestRepository;
import com.recruitease.repository.TestQuestionRepository;

@RestController
public class AptitudeTestController {

    private final AptitudeTestRepository aptitudeTestRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final ApplicationRepository applicationRepository;

    public AptitudeTestController(
            AptitudeTestRepository aptitudeTestRepository,
            TestQuestionRepository testQuestionRepository,
            ApplicationRepository applicationRepository
    ) {
        this.aptitudeTestRepository = aptitudeTestRepository;
        this.testQuestionRepository = testQuestionRepository;
        this.applicationRepository = applicationRepository;
    }

    /* ================= GET ALL TESTS ================= */

    @GetMapping("/api/aptitude-tests")
    public List<AptitudeTest> getAllTests() {
        return aptitudeTestRepository.findAll();
    }

    /* ================= CREATE TEST ================= */

    @PostMapping("/api/aptitude-tests")
    public ResponseEntity<?> addTest(@RequestBody AptitudeTest test) {

        if (test.getTestName() == null ||
                test.getTestName().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Test name is required.");
        }

        if (test.getDuration() == null || test.getDuration() <= 0) {
            return ResponseEntity.badRequest()
                    .body("Test duration must be greater than 0.");
        }

        test.setTestName(test.getTestName().trim());

        if (test.getTestType() == null ||
                test.getTestType().trim().isEmpty()) {

            test.setTestType("Online");
        }

        if (test.getStatus() == null ||
                test.getStatus().trim().isEmpty()) {

            test.setStatus("Pending");
        }

        if (test.getScore() == null) {
            test.setScore(0);
        }

        AptitudeTest savedTest = aptitudeTestRepository.save(test);

        return ResponseEntity.ok(savedTest);
    }

    /* ================= DELETE TEST ================= */

    @DeleteMapping("/api/aptitude-tests/{testId}")
    public ResponseEntity<?> deleteTest(@PathVariable Long testId) {

        AptitudeTest test = aptitudeTestRepository
                .findById(testId)
                .orElse(null);

        if (test == null) {
            return ResponseEntity.notFound().build();
        }

        boolean testIsScheduledForStudent = applicationRepository
                .findAll()
                .stream()
                .anyMatch(application ->
                        application.getTestId() != null &&
                        application.getTestId().equals(testId)
                );

        if (testIsScheduledForStudent) {
            return ResponseEntity.badRequest().body(
                    "This test is already scheduled for a student, so it cannot be deleted."
            );
        }

        List<TestQuestion> questions =
                testQuestionRepository.findByTestId(testId);

        if (!questions.isEmpty()) {
            testQuestionRepository.deleteAll(questions);
        }

        aptitudeTestRepository.deleteById(testId);

        return ResponseEntity.ok(
                "Test and its questions deleted successfully."
        );
    }
}