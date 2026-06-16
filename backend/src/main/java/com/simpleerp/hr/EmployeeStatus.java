package com.simpleerp.hr;

/**
 * Employment state of an employee.
 *
 * <p>Only {@code ACTIVE} and {@code TERMINATED} are ever stored. {@code ON_LEAVE} is derived for
 * display when an employee has APPROVED leave covering the as-of date — it is never persisted.
 */
public enum EmployeeStatus {
    ACTIVE, ON_LEAVE, TERMINATED
}
