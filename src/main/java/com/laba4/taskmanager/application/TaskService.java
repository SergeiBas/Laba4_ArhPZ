package com.laba4.taskmanager.application;

import com.laba4.taskmanager.domain.Task;
import com.laba4.taskmanager.domain.TaskNotFoundException;
import com.laba4.taskmanager.domain.TaskRepository;
import com.laba4.taskmanager.domain.TaskStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Task createTask(String title, String description) {
        String normalizedTitle = title == null ? "" : title.trim();
        String normalizedDescription = description == null ? "" : description.trim();
        if (normalizedTitle.isEmpty() || normalizedDescription.isEmpty()) {
            throw new IllegalArgumentException("Title and description must not be empty");
        }
        return repository.create(normalizedTitle, normalizedDescription);
    }

    public Task getTask(Long id) {
        return repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }

    public List<Task> getTasks(TaskStatus status) {
        return repository.findAll(status);
    }

    public Task updateStatus(Long id, TaskStatus status) {
        repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        return repository.updateStatus(id, status);
    }

    public void deleteTask(Long id) {
        if (!repository.delete(id)) {
            throw new TaskNotFoundException(id);
        }
    }
}
