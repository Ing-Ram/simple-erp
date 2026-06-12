package com.simpleerp.hr;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Data access for leave requests, including HR dashboard aggregations and overlap checks. */
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    /** Count of requests in a given status (e.g. PENDING for the KPI). */
    long countByStatus(LeaveStatus status);

    /** Pending requests as dashboard rows, oldest first so the longest-waiting surface at the top. */
    @Query("""
            select new com.simpleerp.hr.PendingLeave(
                       r.id, r.employee.id, r.employee.name, r.type, r.startDate, r.endDate, r.createdAt)
            from LeaveRequest r
            where r.status = com.simpleerp.hr.LeaveStatus.PENDING
            order by r.createdAt asc
            """)
    List<PendingLeave> pendingOldestFirst();

    /** Approved leave overlapping the window [today, until], soonest-starting first ("who's out"). */
    @Query("""
            select new com.simpleerp.hr.OutToday(
                       r.employee.id, r.employee.name, r.type, r.startDate, r.endDate)
            from LeaveRequest r
            where r.status = com.simpleerp.hr.LeaveStatus.APPROVED
              and r.startDate <= :until and r.endDate >= :today
            order by r.startDate asc
            """)
    List<OutToday> whosOut(@Param("today") LocalDate today, @Param("until") LocalDate until);

    /** Employee ids with approved leave covering the given day — used to derive ON_LEAVE status. */
    @Query("""
            select distinct r.employee.id
            from LeaveRequest r
            where r.status = com.simpleerp.hr.LeaveStatus.APPROVED
              and r.startDate <= :day and r.endDate >= :day
            """)
    List<Long> employeeIdsOnLeave(@Param("day") LocalDate day);

    /** Count of an employee's approved leave overlapping [start, end] — enforces no double-booking. */
    @Query("""
            select count(r)
            from LeaveRequest r
            where r.employee.id = :employeeId
              and r.status = com.simpleerp.hr.LeaveStatus.APPROVED
              and r.startDate <= :end and r.endDate >= :start
            """)
    long countApprovedOverlapping(
            @Param("employeeId") Long employeeId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}
