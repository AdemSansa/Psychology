package Entities;

import java.time.LocalDateTime;

public class Question {
    
        private Long id;

        private int quizId;

        private String questionText;

        private int orderIndex;

        private String imagePath;

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
                String imagePath,
                int quiz,
                String questionText,
                int orderIndex,

                boolean required
        ) {
            this.quizId = quiz;
            this.imagePath = imagePath;
            this.questionText = questionText;
            this.orderIndex = orderIndex;


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