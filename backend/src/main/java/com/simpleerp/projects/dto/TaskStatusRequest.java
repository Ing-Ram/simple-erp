package com.simpleerp.projects.dto;

import com.simpleerp.projects.TaskStatus;
import jakarta.validation.constraints.NotNull;

/** Client payload for changing a task's status. */
public record TaskStatusRequest(@NotNull TaskStatus status) {
}
