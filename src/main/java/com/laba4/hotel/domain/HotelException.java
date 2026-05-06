package com.laba4.hotel.domain;

/**
 * Domain-level exception that carries an HTTP status for the presentation layer.
 * This is intentionally simple for the lab project.
 */
public class HotelException extends RuntimeException {
    private final int status;
    private final String code;

    public HotelException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}

