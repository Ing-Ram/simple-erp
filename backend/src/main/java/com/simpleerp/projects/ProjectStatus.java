package com.simpleerp.projects;

/** Lifecycle of a project. Time entries lock once a project is COMPLETED or CANCELLED. */
public enum ProjectStatus {
    PLANNED, ACTIVE, ON_HOLD, COMPLETED, CANCELLED
}
