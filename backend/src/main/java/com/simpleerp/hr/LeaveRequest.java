package com.simpleerp.hr;

import com.simpleerp.shared.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;

/** A request for time off, reviewed by HR. */
@Entity
@Table(name = "leave_requests")
public class LeaveRequest extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private LeaveType type;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status = LeaveStatus.PENDING;

    /** Who decided the request; null until approved or rejected. */
    private String reviewer;

    /** When the request was decided; null while PENDING. */
    private Instant decidedAt;

    /**
     * Number of business days the leave covers, inclusive of both ends, excluding weekends.
     * Holiday calendars are intentionally out of scope in v1 — only Saturday and Sunday are skipped.
     */
    public long businessDays() {
        long count = 0;
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            DayOfWeek day = d.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                count++;
            }
        }
        return count;
    }

    /** True when this leave is approved and its window covers the given day. */
    public boolean coversWhenApproved(LocalDate day) {
        return status == LeaveStatus.APPROVED && !day.isBefore(startDate) && !day.isAfter(endDate);
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LeaveType getType() {
        return type;
    }

    public void setType(LeaveType type) {
        this.type = type;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }
}
