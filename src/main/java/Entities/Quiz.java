package Entities;

import java.time.LocalDateTime;
import java.util.List;

public class Quiz {


    private Long id;

    /* =======================
       Columns
       ======================= */


    private String title;


    private String description;


    private String category; // Stress, Anxiety, Mood...


    private int totalQuestions = 0;


    private boolean active = true;


    private int minScore = 0;


    private int maxScore = 0;


    private LocalDateTime createdAt;


    private LocalDateTime updatedAt;

    /* =======================
       Relations
       ======================= */


    private List<Question> questions;

    /* =======================
       Constructors
       ======================= */

    public Quiz() {
    }

    public Quiz(String title, String category) {
        this.title = title;
        this.category = category;
    }

    public Quiz(
            String title,
            String description,
            String category,
            boolean active
    ) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.active = active;
    }



    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getMinScore() {
        return minScore;
    }

    public void setMinScore(int minScore) {
        this.minScore = minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
        this.totalQuestions = (questions != null) ? questions.size() : 0;
    }
}
