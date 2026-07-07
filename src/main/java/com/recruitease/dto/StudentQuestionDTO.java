package com.recruitease.dto;

public class StudentQuestionDTO {

    private Long id;
    private Long testId;
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private Integer marks;

    public StudentQuestionDTO(Long id, Long testId,
                              String question,
                              String optionA,
                              String optionB,
                              String optionC,
                              String optionD,
                              Integer marks) {

        this.id = id;
        this.testId = testId;
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.marks = marks;
    }

    public Long getId() {
        return id;
    }

    public Long getTestId() {
        return testId;
    }

    public String getQuestion() {
        return question;
    }

    public String getOptionA() {
        return optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public Integer getMarks() {
        return marks;
    }
}