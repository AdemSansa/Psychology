package Entities;

import java.time.LocalDateTime;

public class Registration {

    private int idRegistration;
    private int eventId;
    private String participantName;
    private String participantEmail;    // NEW
    private String participantPhone;    // NEW
    private String participantNotes;    // NEW
    private String status;
    private LocalDateTime registrationDate;
    private String qrCode;

    public Registration() {}

    public Registration(int id, int eventId, String name, String status,
                        LocalDateTime date, String qr) {
        this.idRegistration = id;
        this.eventId = eventId;
        this.participantName = name;
        this.status = status;
        this.registrationDate = date;
        this.qrCode = qr;
    }

    public int getIdRegistration() { return idRegistration; }
    public void setIdRegistration(int id) { this.idRegistration = id; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getParticipantName() { return participantName; }
    public void setParticipantName(String name) { this.participantName = name; }

    public String getParticipantEmail() { return participantEmail; }
    public void setParticipantEmail(String email) { this.participantEmail = email; }

    public String getParticipantPhone() { return participantPhone; }
    public void setParticipantPhone(String phone) { this.participantPhone = phone; }

    public String getParticipantNotes() { return participantNotes; }
    public void setParticipantNotes(String notes) { this.participantNotes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime date) { this.registrationDate = date; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qr) { this.qrCode = qr; }
}
