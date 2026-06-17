package com.simpleerp.projects;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for projects. */
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /** Projects in a given status (e.g. ACTIVE for the budget chart). */
    List<Project> findByStatus(ProjectStatus status);

    /** Count of projects in a status (e.g. ACTIVE for the KPI). */
    long countByStatus(ProjectStatus status);

    /** Projects past their target end date that are still open — overdue, for needs-attention. */
    List<Project> findByStatusNotInAndTargetEndDateBefore(
            Collection<ProjectStatus> excludedStatuses, LocalDate date);
}
