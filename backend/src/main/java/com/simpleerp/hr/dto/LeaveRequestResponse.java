package com.simpleerp.hr.dto;

import com.simpleerp.hr.LeaveRequest;
import com.simpleerp.hr.LeaveStatus;
import com.simpleerp.hr.LeaveType;
import java.time.Instant;
import java.time.LocalDate;

/** Leave request representation returned to clients. */
public record LeaveRequestResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LeaveType type,
        LocalDate startDate,
        LocalDate endDate,
        long businessDays,
        LeaveStatus status,
        String reviewer,
        Instant decidedAt) {

    /** Maps an entity to its response shape; the single home for this mapping. */
    public static LeaveRequestResponse from(LeaveRequest r) {
        return new LeaveRequestResponse(
                r.getId(),
                r.getEmployee().getId(),
                r.getEmployee().getName(),
                r.getType(),
                r.getStartDate(),
                r.getEndDate(),
                r.businessDays(),
                r.getStatus(),
                r.getReviewer(),
                r.getDecidedAt());
    }
}
