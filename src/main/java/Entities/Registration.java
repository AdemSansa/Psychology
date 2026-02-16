package Entities;

import java.time.LocalDateTime;

public class Registration {

    private int idRegistration;
    private int eventId;
    private String participantName;
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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime date) { this.registrationDate = date; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qr) { this.qrCode = qr; }
}
