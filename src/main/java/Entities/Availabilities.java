package Entities;

import java.sql.Time;

public class Availabilities {

    private int id;
    private Day day;
    private Time startTime;
    private Time endTime;
    private boolean isAvailable;
    private int therapistId;


    public Availabilities() {}

    public Availabilities(String day, Time startTime, Time endTime, boolean isAvailable, int therapistId) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAvailable = isAvailable;
        this.therapistId = therapistId;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Day getDay() { return day; }
    public void setDay(Day day) { this.day = day; }

    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }

    public Time getEndTime() { return endTime; }
    public void setEndTime(Time endTime) { this.endTime = endTime; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean isAvailable) { this.isAvailable = isAvailable; }

    public int getTherapistId() { return therapistId; }
    public void setTherapistId(int therapistId) { this.therapistId = therapistId; }
}
