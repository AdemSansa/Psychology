package Entities;

import java.time.LocalDateTime;

public class Question {

    private Long id;

    private String questionText;

    private String imagePath;

    private boolean required = true;

    private LocalDateTime createdAt;

    public Question() {
    }

    public Question(
            String questionText,
            int orderIndex) {
        this.questionText = questionText;
    }

    public Question(
            String imagePath,
            String questionText,
            int orderIndex,
            boolean required) {
        this.imagePath = imagePath;
        this.questionText = questionText;
        this.required = required;
    }

    /*
     * =======================
     * Getters & Setters
     * =======================
     */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

}