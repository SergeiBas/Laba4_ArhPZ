package com.laba4.taskmanager.data;

import com.laba4.taskmanager.domain.Task;
import com.laba4.taskmanager.domain.TaskRepository;
import com.laba4.taskmanager.domain.TaskStatus;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryTaskRepository implements TaskRepository {
    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    @Override
    public Task create(String title, String description) {
        Long id = seq.getAndIncrement();
        Task task = new Task(id, title, description, TaskStatus.TODO, Instant.now());
        tasks.put(id, task);
        return task;
    }

    @Override
    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<Task> findAll(TaskStatus status) {
        List<Task> all = new ArrayList<>(tasks.values());
        if (status == null) {
            return all;
        }
        return all.stream().filter(t -> t.getStatus() == status).toList();
    }

    @Override
    public Task updateStatus(Long id, TaskStatus status) {
        Task task = tasks.get(id);
        task.setStatus(status);
        return task;
    }

    @Override
    public boolean delete(Long id) {
        return tasks.remove(id) != null;
    }
}
