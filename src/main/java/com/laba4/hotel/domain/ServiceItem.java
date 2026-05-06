package com.laba4.hotel.domain;

public class ServiceItem {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private double pricePerUnit;
    private String currency;

    public ServiceItem(Long id, String name, String description, boolean active, double pricePerUnit, String currency) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
        this.pricePerUnit = pricePerUnit;
        this.currency = currency;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public String getCurrency() {
        return currency;
    }
}

