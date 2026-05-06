package com.laba4.hotel.domain;

public class RoomType {
    private Long id;
    private String name;
    private String description;
    private int baseCapacity;
    private int maxCapacity;
    private boolean active;

    public RoomType(Long id, String name, String description, int baseCapacity, int maxCapacity, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.baseCapacity = baseCapacity;
        this.maxCapacity = maxCapacity;
        this.active = active;
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

    public int getBaseCapacity() {
        return baseCapacity;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public boolean isActive() {
        return active;
    }
}

