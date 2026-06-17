package com.simpleerp.projects;

import com.simpleerp.projects.dto.TaskRequest;
import com.simpleerp.projects.dto.TaskResponse;
import com.simpleerp.projects.dto.TaskStatusRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for tasks. */
@RestController
@RequestMapping("/api/v1/projects/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    /** Lists tasks on a project. */
    @GetMapping
    public List<TaskResponse> list(@RequestParam Long projectId) {
        return service.listByProject(projectId);
    }

    /** Creates a task and returns 201 with its location. */
    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        TaskResponse task = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/projects/tasks/" + task.id())).body(task);
    }

    /** Changes a task's status. */
    @PostMapping("/{id}/status")
    public TaskResponse changeStatus(@PathVariable Long id, @Valid @RequestBody TaskStatusRequest request) {
        return service.changeStatus(id, request.status());
    }

    /** Assigns (or, with no employeeId, unassigns) the task. */
    @PostMapping("/{id}/assign")
    public TaskResponse assign(@PathVariable Long id, @RequestParam(required = false) Long employeeId) {
        return service.assign(id, employeeId);
    }
}
