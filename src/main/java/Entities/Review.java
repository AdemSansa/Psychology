package Entities;

import java.time.LocalDateTime;

public class Review {

    private int idReview;
    private String content;
    private LocalDateTime createdAt;
    private Integer idUser;

    // ===== Constructors =====

    public Review() {}

    // Constructor for INSERT (without id & createdAt)
    public Review(String content, Integer idUser) {
        this.content = content;
        this.idUser = idUser;
    }

    // Full constructor (for SELECT)
    public Review(int idReview, String content,
                  LocalDateTime createdAt, Integer idUser) {

        this.idReview = idReview;
        this.content = content;
        this.createdAt = createdAt;
        this.idUser = idUser;
    }

    // ===== Getters & Setters =====

    public int getIdReview() {
        return idReview;
    }

    public void setIdReview(int idReview) {
        this.idReview = idReview;
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

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }
}
