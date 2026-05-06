package com.laba4.hotel.domain;

import java.time.LocalDate;

public class Booking {
    private Long id;
    private Long roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int guestsCount;
    private String guestNotes;

    // "pending", "active", "completed"
    private String status;

    public Booking(Long id,
                    Long roomId,
                    LocalDate checkIn,
                    LocalDate checkOut,
                    int guestsCount,
                    String guestNotes,
                    String status) {
        this.id = id;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guestsCount = guestsCount;
        this.guestNotes = guestNotes;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public int getGuestsCount() {
        return guestsCount;
    }

    public String getGuestNotes() {
        return guestNotes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

