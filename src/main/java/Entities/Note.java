package Entities;

import java.time.LocalDateTime;

public class Note {

    private int id;
    private String content;
    private String mood;          // new field
    private LocalDateTime createdAt;
    private int appointmentId;
    private int therapistId;

    public Note() {}

    public Note(String content, String mood, int appointmentId, int therapistId) {
        this.content = content;
        this.mood = mood;
        this.appointmentId = appointmentId;
        this.therapistId = therapistId;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public int getTherapistId() { return therapistId; }
    public void setTherapistId(int therapistId) { this.therapistId = therapistId; }
}
