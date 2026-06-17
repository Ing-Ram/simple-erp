package com.simpleerp.projects;

import com.simpleerp.shared.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A checkpoint on a project. A project can only complete once every milestone is completed or
 * explicitly waived — that forces the conversation rather than blocking silently.
 */
@Entity
@Table(name = "milestones")
public class Milestone extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    private String name;
    private LocalDate dueDate;

    /** Set when the milestone is marked complete; null otherwise. */
    private Instant completedAt;

    /** True when the milestone was explicitly waived so the project could complete without it. */
    private boolean waived = false;

    /** Resolved (won't block completion) when it is completed or waived. */
    public boolean isResolved() {
        return completedAt != null || waived;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public boolean isWaived() {
        return waived;
    }

    public void setWaived(boolean waived) {
        this.waived = waived;
    }
}
