package com.recruitease.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitease.dto.SubmitTestRequest;
import com.recruitease.entity.Application;
import com.recruitease.entity.Company;
import com.recruitease.entity.StudentTestResult;
import com.recruitease.entity.TestQuestion;
import com.recruitease.repository.ApplicationRepository;
import com.recruitease.repository.CompanyRepository;
import com.recruitease.repository.StudentTestResultRepository;
import com.recruitease.repository.TestQuestionRepository;

@RestController
@RequestMapping("/api/test-results")
public class StudentTestResultController {

    private final StudentTestResultRepository resultRepository;
    private final CompanyRepository companyRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final ApplicationRepository applicationRepository;

    public StudentTestResultController(StudentTestResultRepository resultRepository,
                                       CompanyRepository companyRepository,
                                       TestQuestionRepository testQuestionRepository,
                                       ApplicationRepository applicationRepository) {

        this.resultRepository = resultRepository;
        this.companyRepository = companyRepository;
        this.testQuestionRepository = testQuestionRepository;
        this.applicationRepository = applicationRepository;
    }

    @GetMapping("/student/{studentId}")
    public List<StudentTestResult> getStudentResults(@PathVariable Long studentId) {
        return resultRepository.findByStudentId(studentId);
    }
   @PostMapping("/submit")
public StudentTestResult submitTest(@RequestBody SubmitTestRequest request) {

    List<TestQuestion> questions =
            testQuestionRepository.findByTestId(request.getTestId());

    Map<Long, String> studentAnswers = request.getAnswers();

    int totalQuestions = questions.size();
    int correctAnswers = 0;
    int obtainedMarks = 0;
    int totalMarks = 0;

    for (TestQuestion q : questions) {

        int marks = q.getMarks() != null ? q.getMarks() : 1;
        totalMarks += marks;

        String studentAnswer = studentAnswers.get(q.getId());

        if (studentAnswer != null &&
                studentAnswer.equalsIgnoreCase(q.getCorrectAnswer())) {

            correctAnswers++;
            obtainedMarks += marks;
        }
    }

    double percentage = 0;

    if (totalMarks > 0) {
        percentage = (obtainedMarks * 100.0) / totalMarks;
    }

    StudentTestResult result = new StudentTestResult();

    result.setStudentId(request.getStudentId());
    result.setApplicationId(request.getApplicationId());
    result.setTestId(request.getTestId());

    result.setTotalQuestions(totalQuestions);
    result.setCorrectAnswers(correctAnswers);
    result.setObtainedMarks(obtainedMarks);
    result.setTotalMarks(totalMarks);
    result.setPercentage(percentage);
    double cutoff = 50.0;

Company company = companyRepository.findById(request.getCompanyId()).orElse(null);

if (company != null && company.getAptitudeCutoff() != null) {
    cutoff = company.getAptitudeCutoff();
}

if (percentage >= cutoff) {
    result.setStatus("Qualified");
} else {
    result.setStatus("Not Qualified");
}

    result.setSubmittedAt(LocalDateTime.now());
    Application application =
        applicationRepository.findById(request.getApplicationId()).orElse(null);

if(application != null){

    application.setStatus("Aptitude Completed");

    applicationRepository.save(application);
}

    return resultRepository.save(result);
}
}