package Entities;

import java.time.LocalDateTime;

public class ForumAdminView {

    private Integer reviewId;
    private Integer userId;
    private LocalDateTime reviewDate;
    private String reviewContent;

    private Integer therapistId;
    private String replyContent;
    private LocalDateTime replyDate;

    // 🔹 Constructeur
    public ForumAdminView(Integer reviewId,
                          Integer userId,
                          LocalDateTime reviewDate,
                          String reviewContent,
                          Integer therapistId,
                          String replyContent,
                          LocalDateTime replyDate) {

        this.reviewId = reviewId;
        this.userId = userId;
        this.reviewDate = reviewDate;
        this.reviewContent = reviewContent;
        this.therapistId = therapistId;
        this.replyContent = replyContent;
        this.replyDate = replyDate;
    }

    // 🔹 Getters
    public Integer getReviewId() {
        return reviewId;
    }

    public Integer getUserId() {
        return userId;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public String getReviewContent() {
        return reviewContent;
    }

    public Integer getTherapistId() {
        return therapistId;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public LocalDateTime getReplyDate() {
        return replyDate;
    }

    // 🔹 Setters (optionnels mais recommandés)
    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }

    public void setReviewContent(String reviewContent) {
        this.reviewContent = reviewContent;
    }

    public void setTherapistId(Integer therapistId) {
        this.therapistId = therapistId;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public void setReplyDate(LocalDateTime replyDate) {
        this.replyDate = replyDate;
    }
    public String getReplyContentDisplay() {
        return replyContent != null ? replyContent : "No reply yet";
    }

    public String getReplyDateDisplay() {
        return replyDate != null ? replyDate.toString() : "No reply yet";
    }

}
