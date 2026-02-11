package Entities;

import java.time.LocalDateTime;

public class Registration {

    private int idRegistration;
    private int userId;
    private int eventId;
    private String status; // registered, cancelled, attended
    private LocalDateTime registrationDate;
    private String qrCode;

    // ===== Constructors =====

    public Registration() {}

    // Constructor for INSERT (without id & registrationDate)
    public Registration(int userId, int eventId, String status, String qrCode) {
        this.userId = userId;
        this.eventId = eventId;
        this.status = status;
        this.qrCode = qrCode;
    }

    // Full constructor (for SELECT)
    public Registration(int idRegistration, int userId, int eventId,
                        String status, LocalDateTime registrationDate, String qrCode) {
        this.idRegistration = idRegistration;
        this.userId = userId;
        this.eventId = eventId;
        this.status = status;
        this.registrationDate = registrationDate;
        this.qrCode = qrCode;
    }

    // ===== Getters & Setters =====

    public int getIdRegistration() {
        return idRegistration;
    }

    public void setIdRegistration(int idRegistration) {
        this.idRegistration = idRegistration;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

}
