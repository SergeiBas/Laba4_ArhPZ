package com.laba4.taskmanager.domain;

import java.time.Instant;

public class Task {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Instant createdAt;

    public Task(Long id, String title, String description, TaskStatus status, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setStatus(TaskStatus status) { this.status = status; }
}
