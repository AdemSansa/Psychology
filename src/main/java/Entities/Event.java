package Entities;

import java.time.LocalDateTime;

public class Event {

    private int idEvent;
    private String title;
    private String description;
    private String type; // online, physique, hybride
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
    private String location;
    private Integer maxParticipants;
    private String status; // draft, published, cancelled
    private LocalDateTime createdAt;
    private Integer organizerId;

    // Image URL
    private String imageUrl;

    // ===== Constructors =====

    public Event() {}

    // Constructor for INSERT (without id & createdAt)
    public Event(String title, String description, String type,
                 LocalDateTime dateStart, LocalDateTime dateEnd,
                 String location, Integer maxParticipants,
                 String status, Integer organizerId, String imageUrl) {

        this.title = title;
        this.description = description;
        this.type = type;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.location = location;
        this.maxParticipants = maxParticipants;
        this.status = status;
        this.organizerId = organizerId;
        this.imageUrl = imageUrl;
    }

    // Full constructor (for SELECT)
    public Event(int idEvent, String title, String description, String type,
                 LocalDateTime dateStart, LocalDateTime dateEnd,
                 String location, Integer maxParticipants,
                 String status, LocalDateTime createdAt,
                 Integer organizerId, String imageUrl) {

        this.idEvent = idEvent;
        this.title = title;
        this.description = description;
        this.type = type;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.location = location;
        this.maxParticipants = maxParticipants;
        this.status = status;
        this.createdAt = createdAt;
        this.organizerId = organizerId;
        this.imageUrl = imageUrl;
    }

    // ===== Getters & Setters =====

    public int getIdEvent() {
        return idEvent;
    }

    public void setIdEvent(int idEvent) {
        this.idEvent = idEvent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getDateStart() {
        return dateStart;
    }

    public void setDateStart(LocalDateTime dateStart) {
        this.dateStart = dateStart;
    }

    public LocalDateTime getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(LocalDateTime dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Integer organizerId) {
        this.organizerId = organizerId;
    }

    // ===== Image URL =====

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
