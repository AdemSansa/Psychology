package Entities;

import java.time.LocalDateTime;

public class QuizResult {

    private Long id;

    private User user;

    private Quiz quiz;

    private int score;

    private int Result;

    private String mood;
    private LocalDateTime takenAt;




    public QuizResult() {
    }

    public QuizResult(User user, Quiz quiz, int score) {
        this.user = user;
        this.quiz = quiz;
        this.score = score;
    }

    public Long getId() {
        return id;
    }

    public void setResult(int result) {
        Result = result;
    }
    public void setMood(String mood) {
        this.mood = mood;
    }
    public int getResult() {
        return Result;
    }
    public String getMood() {
        return mood;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
    public LocalDateTime getTakenAt() {
        return takenAt;
    }
    public void setTakenAt(LocalDateTime takenAt) {
        this.takenAt = takenAt;
    }

}
