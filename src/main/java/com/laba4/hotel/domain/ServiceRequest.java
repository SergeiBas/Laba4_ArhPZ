package com.laba4.hotel.domain;

import java.time.Instant;

public class ServiceRequest {
    private Long id;
    private Long bookingId;
    private Long serviceId;
    private int quantity;

    // "new", "in_progress", "done"
    private String status;
    private Instant createdAt;

    public ServiceRequest(Long id, Long bookingId, Long serviceId, int quantity, String status, Instant createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.serviceId = serviceId;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

