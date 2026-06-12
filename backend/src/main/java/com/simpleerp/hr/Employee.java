package com.simpleerp.hr;

import com.simpleerp.shared.AuditableEntity;
import com.simpleerp.shared.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** A person employed by the company. Never hard-deleted — termination preserves history. */
@Entity
@Table(name = "employees")
public class Employee extends AuditableEntity {

    private String name;
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    /** Free-text job title. */
    private String position;

    private LocalDate hireDate;

    /** Null while employed; set on termination so headcount and turnover math stay correct. */
    private LocalDate terminationDate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "salary_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "salary_currency"))})
    private Money salary;

    /** Stored employment state — only ACTIVE or TERMINATED; ON_LEAVE is derived, never stored. */
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    /**
     * Display status as of a day: TERMINATED if terminated, ON_LEAVE if the employee has approved
     * leave covering the day, otherwise ACTIVE. Derived so there is one copy of the truth.
     */
    public EmployeeStatus effectiveStatus(boolean onLeave) {
        if (status == EmployeeStatus.TERMINATED) {
            return EmployeeStatus.TERMINATED;
        }
        return onLeave ? EmployeeStatus.ON_LEAVE : EmployeeStatus.ACTIVE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }

    public Money getSalary() {
        return salary;
    }

    public void setSalary(Money salary) {
        this.salary = salary;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeStatus status) {
        this.status = status;
    }
}
