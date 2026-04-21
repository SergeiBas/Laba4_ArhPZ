package com.laba4.taskmanager.presentation.dto;

import com.laba4.taskmanager.domain.Task;
import com.laba4.taskmanager.domain.TaskStatus;

import java.time.Instant;

public record TaskResponse(Long id, String title, String description, TaskStatus status, Instant createdAt) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getStatus(), task.getCreatedAt());
    }
}
