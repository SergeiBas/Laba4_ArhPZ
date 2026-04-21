package com.laba4.taskmanager.domain;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(Long id) {
        super("Task " + id + " not found");
    }
}
