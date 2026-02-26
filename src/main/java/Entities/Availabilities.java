package Entities;

import java.sql.Time;

public class Availabilities {

    private int id;
    private Day day;
    private Time startTime;
    private Time endTime;
    private boolean isAvailable;
    private int therapistId;
    private java.sql.Date specificDate;

    public Availabilities() {
    }

    public Availabilities(String day, Time startTime, Time endTime, boolean isAvailable, int therapistId,
            java.sql.Date specificDate) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAvailable = isAvailable;
        this.therapistId = therapistId;
        this.specificDate = specificDate;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public int getTherapistId() {
        return therapistId;
    }

    public void setTherapistId(int therapistId) {
        this.therapistId = therapistId;
    }

    public java.sql.Date getSpecificDate() {
        return specificDate;
    }

    public void setSpecificDate(java.sql.Date specificDate) {
        this.specificDate = specificDate;
    }
}
