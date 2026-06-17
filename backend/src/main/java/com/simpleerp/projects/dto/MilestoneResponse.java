package com.simpleerp.projects.dto;

import com.simpleerp.projects.Milestone;
import java.time.Instant;
import java.time.LocalDate;

/** Milestone representation; {@code resolved} is true once completed or waived. */
public record MilestoneResponse(
        Long id,
        Long projectId,
        String name,
        LocalDate dueDate,
        Instant completedAt,
        boolean waived,
        boolean resolved) {

    /** Maps an entity to its response shape; the single home for this mapping. */
    public static MilestoneResponse from(Milestone m) {
        return new MilestoneResponse(
                m.getId(),
                m.getProject().getId(),
                m.getName(),
                m.getDueDate(),
                m.getCompletedAt(),
                m.isWaived(),
                m.isResolved());
    }
}
