package com.recruitease.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.recruitease.dto.StudentQuestionDTO;
import com.recruitease.entity.TestQuestion;
import com.recruitease.repository.AptitudeTestRepository;
import com.recruitease.repository.TestQuestionRepository;

@RestController
public class TestQuestionController {

    private final TestQuestionRepository testQuestionRepository;
    private final AptitudeTestRepository aptitudeTestRepository;

    public TestQuestionController(
            TestQuestionRepository testQuestionRepository,
            AptitudeTestRepository aptitudeTestRepository
    ) {
        this.testQuestionRepository = testQuestionRepository;
        this.aptitudeTestRepository = aptitudeTestRepository;
    }

    @PostMapping("/api/test-questions")
    public ResponseEntity<?> addQuestion(@RequestBody TestQuestion question) {

        if (question.getTestId() == null) {
            return ResponseEntity.badRequest()
                    .body("Test ID is required.");
        }

        if (!aptitudeTestRepository.existsById(question.getTestId())) {
            return ResponseEntity.badRequest()
                    .body("This Test ID does not exist.");
        }

        if (question.getQuestion() == null || question.getQuestion().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Question is required.");
        }

        if (testQuestionRepository.existsByTestIdAndQuestionIgnoreCase(
                question.getTestId(),
                question.getQuestion().trim()
        )) {
            return ResponseEntity.badRequest()
                    .body("Same question already exists in this Test ID.");
        }

        if (question.getMarks() == null || question.getMarks() <= 0) {
            question.setMarks(1);
        }

        return ResponseEntity.ok(
                testQuestionRepository.save(question)
        );
    }

    // Admin: correct answer visible
    @GetMapping("/api/test-questions/{testId}")
    public List<TestQuestion> getQuestionsByTest(@PathVariable Long testId) {
        return testQuestionRepository.findByTestId(testId);
    }

    // Student: correct answer hidden
    @GetMapping("/api/test-questions/student/{testId}")
    public List<StudentQuestionDTO> getStudentQuestions(
            @PathVariable Long testId
    ) {
        return testQuestionRepository.findByTestId(testId)
                .stream()
                .map(question -> new StudentQuestionDTO(
                        question.getId(),
                        question.getTestId(),
                        question.getQuestion(),
                        question.getOptionA(),
                        question.getOptionB(),
                        question.getOptionC(),
                        question.getOptionD(),
                        question.getMarks()
                ))
                .toList();
    }

    /*
     * Example:
     * Source Test ID: 1
     * Target Test ID: 6
     *
     * All questions from Test ID 1 will copy into Test ID 6.
     * Original Test ID 1 questions remain unchanged.
     */
    @PostMapping("/api/test-questions/copy")
    public ResponseEntity<?> copyQuestions(
            @RequestParam Long sourceTestId,
            @RequestParam Long targetTestId
    ) {
        if (sourceTestId.equals(targetTestId)) {
            return ResponseEntity.badRequest()
                    .body("Source Test ID and Target Test ID cannot be same.");
        }

        if (!aptitudeTestRepository.existsById(sourceTestId)) {
            return ResponseEntity.badRequest()
                    .body("Source Test ID does not exist.");
        }

        if (!aptitudeTestRepository.existsById(targetTestId)) {
            return ResponseEntity.badRequest()
                    .body("Target Test ID does not exist.");
        }

        List<TestQuestion> sourceQuestions =
                testQuestionRepository.findByTestId(sourceTestId);

        if (sourceQuestions.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Source Test ID has no questions to copy.");
        }

        int copiedCount = 0;
        int skippedCount = 0;

        for (TestQuestion sourceQuestion : sourceQuestions) {

            boolean duplicateExists =
                    testQuestionRepository.existsByTestIdAndQuestionIgnoreCase(
                            targetTestId,
                            sourceQuestion.getQuestion()
                    );

            if (duplicateExists) {
                skippedCount++;
                continue;
            }

            TestQuestion copiedQuestion = new TestQuestion();

            copiedQuestion.setTestId(targetTestId);
            copiedQuestion.setQuestion(sourceQuestion.getQuestion());
            copiedQuestion.setOptionA(sourceQuestion.getOptionA());
            copiedQuestion.setOptionB(sourceQuestion.getOptionB());
            copiedQuestion.setOptionC(sourceQuestion.getOptionC());
            copiedQuestion.setOptionD(sourceQuestion.getOptionD());
            copiedQuestion.setCorrectAnswer(sourceQuestion.getCorrectAnswer());
            copiedQuestion.setMarks(sourceQuestion.getMarks());

            testQuestionRepository.save(copiedQuestion);

            copiedCount++;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("message", "Questions copied successfully.");
        response.put("sourceTestId", sourceTestId);
        response.put("targetTestId", targetTestId);
        response.put("copiedQuestions", copiedCount);
        response.put("skippedDuplicates", skippedCount);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/test-questions/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {

        if (!testQuestionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        testQuestionRepository.deleteById(id);

        return ResponseEntity.ok("Question deleted successfully.");
    }
}