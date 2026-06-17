package com.simpleerp.projects.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Client payload for creating a milestone. */
public record MilestoneRequest(
        @NotNull Long projectId,
        @NotNull String name,
        LocalDate dueDate) {
}
