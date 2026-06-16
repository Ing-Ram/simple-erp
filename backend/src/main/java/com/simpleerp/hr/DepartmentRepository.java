package com.simpleerp.hr;

import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for departments. */
public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
