package com.simpleerp.projects.dto;

import com.simpleerp.projects.Task;
import com.simpleerp.projects.TaskStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Task representation; the assignee name is resolved by the service. */
public record TaskResponse(
        Long id,
        Long projectId,
        String title,
        Long assigneeEmployeeId,
        String assigneeName,
        TaskStatus status,
        LocalDate dueDate,
        BigDecimal estimateHours) {

    /** Maps an entity to its response shape, given the resolved assignee name (null if unassigned). */
    public static TaskResponse from(Task t, String assigneeName) {
        return new TaskResponse(
                t.getId(),
                t.getProject().getId(),
                t.getTitle(),
                t.getAssigneeEmployeeId(),
                assigneeName,
                t.getStatus(),
                t.getDueDate(),
                t.getEstimateHours());
    }
}
