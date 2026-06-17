package com.simpleerp.projects;

import com.simpleerp.shared.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/** A flat unit of work within a project, optionally assigned to an employee. */
@Entity
@Table(name = "tasks")
public class Task extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    private String title;

    /** Null when unassigned. */
    private Long assigneeEmployeeId;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.TODO;

    private LocalDate dueDate;
    private BigDecimal estimateHours;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getAssigneeEmployeeId() {
        return assigneeEmployeeId;
    }

    public void setAssigneeEmployeeId(Long assigneeEmployeeId) {
        this.assigneeEmployeeId = assigneeEmployeeId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getEstimateHours() {
        return estimateHours;
    }

    public void setEstimateHours(BigDecimal estimateHours) {
        this.estimateHours = estimateHours;
    }
}
