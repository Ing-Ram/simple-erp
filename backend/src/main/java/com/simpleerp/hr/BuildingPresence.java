package com.simpleerp.hr;

import com.simpleerp.shared.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A single check-in for the day: when an employee arrived (on site or remote) and, once they leave,
 * when they checked out. The basis for the emergency roll-call.
 */
@Entity
@Table(name = "building_presence")
public class BuildingPresence extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private WorkMode workMode;

    private Instant checkInAt;

    /** Null while the employee is still checked in. */
    private Instant checkOutAt;

    /** True when this is an on-site check-in with no check-out yet — i.e. in the building now. */
    public boolean isOnSiteOpen() {
        return workMode == WorkMode.ON_SITE && checkOutAt == null;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public WorkMode getWorkMode() {
        return workMode;
    }

    public void setWorkMode(WorkMode workMode) {
        this.workMode = workMode;
    }

    public Instant getCheckInAt() {
        return checkInAt;
    }

    public void setCheckInAt(Instant checkInAt) {
        this.checkInAt = checkInAt;
    }

    public Instant getCheckOutAt() {
        return checkOutAt;
    }

    public void setCheckOutAt(Instant checkOutAt) {
        this.checkOutAt = checkOutAt;
    }
}
