package com.laba4.taskmanager.presentation;

import com.laba4.taskmanager.application.TaskService;
import com.laba4.taskmanager.domain.TaskStatus;
import com.laba4.taskmanager.presentation.dto.CreateTaskRequest;
import com.laba4.taskmanager.presentation.dto.TaskResponse;
import com.laba4.taskmanager.presentation.dto.UpdateStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody CreateTaskRequest request) {
        return TaskResponse.from(service.createTask(request.getTitle(), request.getDescription()));
    }

    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable Long id) {
        return TaskResponse.from(service.getTask(id));
    }

    @GetMapping
    public List<TaskResponse> list(@RequestParam(required = false) TaskStatus status) {
        return service.getTasks(status).stream().map(TaskResponse::from).toList();
    }

    @PatchMapping("/{id}/status")
    public TaskResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        return TaskResponse.from(service.updateStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteTask(id);
    }
}
