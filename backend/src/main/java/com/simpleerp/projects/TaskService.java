package com.simpleerp.projects;

import com.simpleerp.hr.EmployeeService;
import com.simpleerp.projects.dto.TaskRequest;
import com.simpleerp.projects.dto.TaskResponse;
import com.simpleerp.shared.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Task management: create, assign, and move status. Tasks are flat — no subtasks in v1. */
@Service
@Transactional
public class TaskService {

    private final TaskRepository tasks;
    private final ProjectService projects;
    private final EmployeeService employees;

    public TaskService(TaskRepository tasks, ProjectService projects, EmployeeService employees) {
        this.tasks = tasks;
        this.projects = projects;
        this.employees = employees;
    }

    /** Creates a TODO task on a project, optionally assigned. */
    public TaskResponse create(TaskRequest request) {
        Project project = projects.load(request.projectId());
        if (request.assigneeEmployeeId() != null) {
            employees.get(request.assigneeEmployeeId());
        }
        Task task = new Task();
        task.setProject(project);
        task.setTitle(request.title());
        task.setAssigneeEmployeeId(request.assigneeEmployeeId());
        task.setDueDate(request.dueDate());
        task.setEstimateHours(request.estimateHours());
        task.setStatus(TaskStatus.TODO);
        return toResponse(tasks.save(task));
    }

    /** Moves a task to a new status (TODO / IN_PROGRESS / DONE). */
    public TaskResponse changeStatus(Long id, TaskStatus status) {
        Task task = load(id);
        task.setStatus(status);
        return toResponse(task);
    }

    /** Assigns (or, with null, unassigns) the task. */
    public TaskResponse assign(Long id, Long employeeId) {
        Task task = load(id);
        if (employeeId != null) {
            employees.get(employeeId);
        }
        task.setAssigneeEmployeeId(employeeId);
        return toResponse(task);
    }

    /** Tasks on one project. */
    @Transactional(readOnly = true)
    public List<TaskResponse> listByProject(Long projectId) {
        return tasks.findByProject_Id(projectId).stream().map(this::toResponse).toList();
    }

    private Task load(Long id) {
        return tasks.findById(id).orElseThrow(() -> new NotFoundException("Task", id));
    }

    private TaskResponse toResponse(Task t) {
        String assigneeName = t.getAssigneeEmployeeId() == null
                ? null : employees.get(t.getAssigneeEmployeeId()).name();
        return TaskResponse.from(t, assigneeName);
    }
}
