package com.simpleerp.projects;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Data access for milestones. */
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    /** Milestones on one project. */
    List<Milestone> findByProject_Id(Long projectId);

    /** Unresolved milestones on a project (neither completed nor waived) — block project completion. */
    long countByProject_IdAndCompletedAtIsNullAndWaivedFalse(Long projectId);

    /**
     * Unresolved milestones due on or before a date, soonest first — the dashboard's upcoming panel
     * (overdue ones fall in this set too and are flagged by the service).
     */
    @Query("""
            select m from Milestone m
            where m.completedAt is null and m.waived = false and m.dueDate <= :until
            order by m.dueDate asc
            """)
    List<Milestone> upcomingUnresolved(@Param("until") LocalDate until);
}
