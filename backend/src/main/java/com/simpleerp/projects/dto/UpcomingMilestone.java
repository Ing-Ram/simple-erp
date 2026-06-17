package com.simpleerp.projects.dto;

import java.time.LocalDate;

/** An unresolved milestone due soon; {@code overdue} flags ones already past their date. */
public record UpcomingMilestone(Long id, String projectName, String name, LocalDate dueDate, boolean overdue) {
}
