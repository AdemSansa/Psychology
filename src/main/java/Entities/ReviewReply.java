package Entities;

import java.time.LocalDateTime;

public class ReviewReply {

    private int idReply;
    private String content;
    private LocalDateTime createdAt;
    private Integer reviewId;
    private Integer idTherapist;

    // ===== Constructors =====

    public ReviewReply() {}

    // Constructor for INSERT (without id & createdAt)
    public ReviewReply(String content, Integer reviewId,
                       Integer idTherapist) {

        this.content = content;
        this.reviewId = reviewId;
        this.idTherapist = idTherapist;
    }

    // Full constructor (for SELECT)
    public ReviewReply(int idReply, String content,
                       LocalDateTime createdAt, Integer reviewId,
                       Integer idTherapist) {

        this.idReply = idReply;
        this.content = content;
        this.createdAt = createdAt;
        this.reviewId = reviewId;
        this.idTherapist = idTherapist;
    }

    // ===== Getters & Setters =====

    public int getIdReply() {
        return idReply;
    }

    public void setIdReply(int idReply) {
        this.idReply = idReply;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getReviewId() {
        return reviewId;
    }

    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
    }

    public Integer getIdTherapist() {
        return idTherapist;
    }

    public void setIdTherapist(Integer idTherapist) {
        this.idTherapist = idTherapist;
    }
}
