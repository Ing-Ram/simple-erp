package com.simpleerp.hr;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Data access for employees, including HR dashboard aggregations. */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /** Headcount of currently-employed people (not terminated). */
    long countByTerminationDateIsNull();

    /** Employees hired on or after the given date. */
    long countByHireDateGreaterThanEqual(LocalDate since);

    /** Employees terminated on or after the given date. */
    long countByTerminationDateGreaterThanEqual(LocalDate since);

    /** Active headcount grouped by department, largest first. */
    @Query("""
            select new com.simpleerp.hr.DepartmentHeadcount(e.department.name, count(e))
            from Employee e
            where e.terminationDate is null
            group by e.department.name
            order by count(e) desc
            """)
    List<DepartmentHeadcount> headcountByDepartment();
}
