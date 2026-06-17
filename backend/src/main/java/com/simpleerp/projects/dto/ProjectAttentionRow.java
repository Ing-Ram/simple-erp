package com.simpleerp.projects.dto;

import java.time.LocalDate;

/**
 * A "needs attention" row: {@code kind} is {@code "TASK"} (overdue, not done) or {@code "PROJECT"}
 * (past its target end date, not completed).
 */
public record ProjectAttentionRow(String kind, Long id, String label, String projectName, LocalDate dueDate) {
}
