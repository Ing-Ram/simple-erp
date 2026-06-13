package com.simpleerp.hr.dto;

import com.simpleerp.hr.BuildingPresence;
import com.simpleerp.hr.WorkMode;
import java.time.Instant;

/** A single check-in record as returned to clients. */
public record PresenceResponse(
        Long id,
        Long employeeId,
        String employeeName,
        WorkMode workMode,
        Instant checkInAt,
        Instant checkOutAt) {

    /** Maps an entity to its response shape; the single home for this mapping. */
    public static PresenceResponse from(BuildingPresence p) {
        return new PresenceResponse(
                p.getId(),
                p.getEmployee().getId(),
                p.getEmployee().getName(),
                p.getWorkMode(),
                p.getCheckInAt(),
                p.getCheckOutAt());
    }
}
