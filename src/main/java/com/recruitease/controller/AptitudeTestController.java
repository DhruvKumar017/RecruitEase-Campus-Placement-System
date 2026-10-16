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

    List<AptitudeTest> tests = aptitudeTestRepository.findAll();

    tests.sort((a, b) -> Long.compare(a.getId(), b.getId()));

    boolean changed = false;

    for (int i = 0; i < tests.size(); i++) {
        int correctTestNumber = i + 1;

        if (tests.get(i).getTestNumber() == null ||
                !tests.get(i).getTestNumber().equals(correctTestNumber)) {

            tests.get(i).setTestNumber(correctTestNumber);
            changed = true;
        }
    }

    if (changed) {
        aptitudeTestRepository.saveAll(tests);
    }

    return tests;
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
        List<AptitudeTest> existingTests =
        aptitudeTestRepository.findAllByOrderByTestNumberAsc();

int nextTestNumber = existingTests.size() + 1;

test.setTestNumber(nextTestNumber);

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

var scheduledApplications = applicationRepository.findByTestId(testId);
scheduledApplications.forEach(application -> {
    application.setTestId(null);
    application.setTestDate(null);
    application.setTestStartTime(null);
    application.setTestEndTime(null);
    application.setTestPermission(null);
});

if (!scheduledApplications.isEmpty()) {
    applicationRepository.saveAll(scheduledApplications);
}

        List<TestQuestion> questions =
                testQuestionRepository.findByTestId(testId);

        if (!questions.isEmpty()) {
            testQuestionRepository.deleteAll(questions);
        }

aptitudeTestRepository.deleteById(testId);

List<AptitudeTest> remainingTests =
        aptitudeTestRepository.findAll();

remainingTests.sort((a, b) ->
        Long.compare(a.getId(), b.getId())
);

for (int i = 0; i < remainingTests.size(); i++) {
    remainingTests.get(i).setTestNumber(i + 1);
}

aptitudeTestRepository.saveAll(remainingTests);

return ResponseEntity.ok(
        "Test and its questions deleted successfully."
);
    }
}