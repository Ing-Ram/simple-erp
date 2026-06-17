package com.simpleerp.projects;

import com.simpleerp.hr.EmployeeService;
import com.simpleerp.projects.dto.TimeEntryRequest;
import com.simpleerp.projects.dto.TimeEntryResponse;
import com.simpleerp.shared.InvalidStateException;
import com.simpleerp.shared.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Logs hours against tasks. Time locks once the owning project is COMPLETED or CANCELLED. */
@Service
@Transactional
public class TimeEntryService {

    private final TimeEntryRepository timeEntries;
    private final TaskRepository tasks;
    private final EmployeeService employees;

    public TimeEntryService(TimeEntryRepository timeEntries, TaskRepository tasks, EmployeeService employees) {
        this.timeEntries = timeEntries;
        this.tasks = tasks;
        this.employees = employees;
    }

    /** Records time against a task, rejecting entries once the project is locked. */
    public TimeEntryResponse log(TimeEntryRequest request) {
        Task task = tasks.findById(request.taskId())
                .orElseThrow(() -> new NotFoundException("Task", request.taskId()));
        if (task.getProject().isLocked()) {
            throw new InvalidStateException(
                    "Time is locked: project is " + task.getProject().getStatus());
        }
        employees.get(request.employeeId());
        TimeEntry entry = new TimeEntry();
        entry.setTask(task);
        entry.setEmployeeId(request.employeeId());
        entry.setEntryDate(request.entryDate());
        entry.setHours(request.hours());
        entry.setNote(request.note());
        return toResponse(timeEntries.save(entry));
    }

    /** Time entries on one task, newest first. */
    @Transactional(readOnly = true)
    public List<TimeEntryResponse> listByTask(Long taskId) {
        return timeEntries.findByTask_IdOrderByEntryDateDesc(taskId).stream().map(this::toResponse).toList();
    }

    private TimeEntryResponse toResponse(TimeEntry e) {
        String employeeName = employees.get(e.getEmployeeId()).name();
        return TimeEntryResponse.from(e, employeeName);
    }
}
