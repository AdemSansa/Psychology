package Entities;

import java.time.LocalDateTime;

public class Question {
    
        private Long id;

        private int quizId;

        private String questionText;

        private int orderIndex;

        private int minValue = 1;

        private int maxValue = 5;

        private String labelMin = "Strongly disagree";

        private String labelMax = "Strongly agree";


        private boolean required = true;


        private LocalDateTime createdAt;



        public Question() {
        }

        public Question(
                int quiz,
                String questionText,
                int orderIndex
        ) {
            this.quizId = quiz;
            this.questionText = questionText;
            this.orderIndex = orderIndex;
        }

        public Question(
                int quiz,
                String questionText,
                int orderIndex,
                int minValue,
                int maxValue,
                String labelMin,
                String labelMax,
                boolean required
        ) {
            this.quizId = quiz;
            this.questionText = questionText;
            this.orderIndex = orderIndex;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.labelMin = labelMin;
            this.labelMax = labelMax;
            this.required = required;
        }

    /* =======================
       Getters & Setters
       ======================= */

        public Long getId() {
            return id;
        }

        public int getQuizId() {
            return quizId;
        }

        public void setQuiz(int quiz) {
            this.quizId = quiz;
        }

        public String getQuestionText() {
            return questionText;
        }

        public void setQuestionText(String questionText) {
            this.questionText = questionText;
        }

        public int getOrderIndex() {
            return orderIndex;
        }

        public void setOrderIndex(int orderIndex) {
            this.orderIndex = orderIndex;
        }

        public int getMinValue() {
            return minValue;
        }

        public void setMinValue(int minValue) {
            this.minValue = minValue;
        }

        public int getMaxValue() {
            return maxValue;
        }

        public void setMaxValue(int maxValue) {
            this.maxValue = maxValue;
        }

        public String getLabelMin() {
            return labelMin;
        }

        public void setLabelMin(String labelMin) {
            this.labelMin = labelMin;
        }

        public String getLabelMax() {
            return labelMax;
        }

        public void setLabelMax(String labelMax) {
            this.labelMax = labelMax;
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
}