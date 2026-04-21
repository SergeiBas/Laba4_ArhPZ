package com.laba4.taskmanager.domain;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task create(String title, String description);
    Optional<Task> findById(Long id);
    List<Task> findAll(TaskStatus status);
    Task updateStatus(Long id, TaskStatus status);
    boolean delete(Long id);
}
