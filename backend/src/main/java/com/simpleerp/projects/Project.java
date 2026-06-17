package com.simpleerp.projects;

import com.simpleerp.shared.AuditableEntity;
import com.simpleerp.shared.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * A unit of work with a budget. Customer is optional (internal projects allowed); customer and
 * manager are referenced by id and resolved through Finance and HR services, never their tables.
 */
@Entity
@Table(name = "projects")
public class Project extends AuditableEntity {

    private String name;

    /** Null for internal projects. */
    private Long customerId;

    private Long managerEmployeeId;

    private LocalDate startDate;
    private LocalDate targetEndDate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "budget_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "budget_currency"))})
    private Money budget;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.PLANNED;

    /** True once the project no longer accepts time entries. */
    public boolean isLocked() {
        return status == ProjectStatus.COMPLETED || status == ProjectStatus.CANCELLED;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getManagerEmployeeId() {
        return managerEmployeeId;
    }

    public void setManagerEmployeeId(Long managerEmployeeId) {
        this.managerEmployeeId = managerEmployeeId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getTargetEndDate() {
        return targetEndDate;
    }

    public void setTargetEndDate(LocalDate targetEndDate) {
        this.targetEndDate = targetEndDate;
    }

    public Money getBudget() {
        return budget;
    }

    public void setBudget(Money budget) {
        this.budget = budget;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }
}
