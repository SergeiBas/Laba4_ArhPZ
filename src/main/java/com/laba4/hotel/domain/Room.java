package com.laba4.hotel.domain;

public class Room {
    private Long id;
    private String number;
    private int floor;
    private Long roomTypeId;
    private String status; // e.g. "available", "occupied", "cleaning"
    private double nightlyRate;
    private String currency;

    public Room(Long id, String number, int floor, Long roomTypeId, String status, double nightlyRate, String currency) {
        this.id = id;
        this.number = number;
        this.floor = floor;
        this.roomTypeId = roomTypeId;
        this.status = status;
        this.nightlyRate = nightlyRate;
        this.currency = currency;
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public int getFloor() {
        return floor;
    }

    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public String getStatus() {
        return status;
    }

    public double getNightlyRate() {
        return nightlyRate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

