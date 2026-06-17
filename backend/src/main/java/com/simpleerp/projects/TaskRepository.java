package com.simpleerp.projects;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for tasks. */
public interface TaskRepository extends JpaRepository<Task, Long> {

    /** Tasks on one project. */
    List<Task> findByProject_Id(Long projectId);

    /** Open tasks past their due date — overdue, for needs-attention. */
    List<Task> findByStatusNotAndDueDateBefore(TaskStatus status, LocalDate date);
}
