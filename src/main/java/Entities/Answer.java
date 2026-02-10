package Entities;

public class Answer {
    private Question question;
    private int value; // 0 → 4


    public QuizCategory getCategory() {
        return question.getCategory();
    }
    public int getValue() {
        return value;
    }
}
