package com.simpleerp.projects;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Data access for time entries, including the hours aggregations behind project cost. */
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {

    /**
     * Hours each employee logged on one project. The service multiplies each by that employee's HR
     * hourly cost to get actual spend — cost can't be summed in SQL because salary lives in HR.
     */
    @Query("""
            select new com.simpleerp.projects.EmployeeHours(te.employeeId, sum(te.hours))
            from TimeEntry te
            where te.task.project.id = :projectId
            group by te.employeeId
            """)
    List<EmployeeHours> hoursByEmployeeForProject(@Param("projectId") Long projectId);

    /** Total hours logged on or after a date (e.g. this week, or last 30 days for utilization). */
    @Query("select coalesce(sum(te.hours), 0) from TimeEntry te where te.entryDate >= :since")
    BigDecimal hoursLoggedSince(@Param("since") LocalDate since);

    /** Time entries on one task, newest first. */
    List<TimeEntry> findByTask_IdOrderByEntryDateDesc(Long taskId);
}
