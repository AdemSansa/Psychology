package Entities;

import java.time.LocalDateTime;

public class Note {

    private int id;
    private String content;
    private LocalDateTime createdAt;
    private int appointmentId;
    private int therapistId;

    public Note() {}

    public Note(String content, int appointmentId, int therapistId) {
        this.content = content;
        this.appointmentId = appointmentId;
        this.therapistId = therapistId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public int getTherapistId() { return therapistId; }
    public void setTherapistId(int therapistId) { this.therapistId = therapistId; }
}
