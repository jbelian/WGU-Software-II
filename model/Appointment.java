package model;

import java.time.LocalDateTime;

/**
 *
 * @author Joseph
 */
public class Appointment {
    private int appointmentId, customerId;
    private String title, type;
    private LocalDateTime start, end;
    
    public Appointment(int customerId, String title, String type, LocalDateTime start, LocalDateTime end) {
        this.customerId = customerId;
        this.title = title;
        this.type = type;
        this.start = start;
        this.end = end;
    }
    
    public int getAppointmentId() {
        return appointmentId;
    }
    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getCustomerId() {
        return customerId;
    }
    public void setCustomerId(int id) {
        this.customerId = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getStart() {
        return start;
    }
    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }
    public void setEnd(LocalDateTime end) {
        this.end = end;
    }
}