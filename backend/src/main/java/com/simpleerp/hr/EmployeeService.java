package com.simpleerp.hr;

import com.simpleerp.hr.dto.EmployeeRequest;
import com.simpleerp.hr.dto.EmployeeResponse;
import com.simpleerp.shared.Money;
import com.simpleerp.shared.NotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Employee management and the cross-module lookup seam.
 *
 * <p>Sales (order owner) and Projects (assignee, time entries) resolve employees through
 * {@link #require(Long)} here, never through {@link EmployeeRepository} directly.
 */
@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employees;
    private final DepartmentRepository departments;
    private final LeaveRequestRepository leave;

    public EmployeeService(EmployeeRepository employees, DepartmentRepository departments,
                           LeaveRequestRepository leave) {
        this.employees = employees;
        this.departments = departments;
        this.leave = leave;
    }

    /** Hires a new active employee. */
    public EmployeeResponse create(EmployeeRequest request) {
        Department department = departments.findById(request.departmentId())
                .orElseThrow(() -> new NotFoundException("Department", request.departmentId()));
        Employee employee = new Employee();
        employee.setName(request.name());
        employee.setEmail(request.email());
        employee.setDepartment(department);
        employee.setPosition(request.position());
        employee.setHireDate(request.hireDate());
        employee.setSalary(new Money(request.salary(), request.currency()));
        employee.setStatus(EmployeeStatus.ACTIVE);
        return EmployeeResponse.from(employees.save(employee), false);
    }

    /** All employees, with ON_LEAVE derived for anyone on approved leave today. */
    @Transactional(readOnly = true)
    public List<EmployeeResponse> list() {
        Set<Long> onLeave = Set.copyOf(leave.employeeIdsOnLeave(LocalDate.now()));
        return employees.findAll().stream()
                .map(e -> EmployeeResponse.from(e, onLeave.contains(e.getId())))
                .toList();
    }

    /** Loads one employee as a response, with ON_LEAVE derived for today. */
    @Transactional(readOnly = true)
    public EmployeeResponse get(Long id) {
        Employee employee = require(id);
        boolean onLeave = leave.employeeIdsOnLeave(LocalDate.now()).contains(id);
        return EmployeeResponse.from(employee, onLeave);
    }

    /** Terminates an employee on the given date, preserving the record for headcount/turnover math. */
    public EmployeeResponse terminate(Long id, LocalDate effectiveDate) {
        Employee employee = require(id);
        employee.setTerminationDate(effectiveDate);
        employee.setStatus(EmployeeStatus.TERMINATED);
        return EmployeeResponse.from(employee, false);
    }

    /** Loads the employee entity or throws 404. The cross-module lookup other modules call. */
    @Transactional(readOnly = true)
    public Employee require(Long id) {
        return employees.findById(id).orElseThrow(() -> new NotFoundException("Employee", id));
    }

    /**
     * The employee's fully-loaded hourly cost, derived from salary ÷ 2080 (40 hours × 52 weeks).
     * The Projects module multiplies this by hours logged to get actual project cost — salary lives
     * only here and is never copied into another module.
     */
    @Transactional(readOnly = true)
    public Money hourlyCost(Long id) {
        Money salary = require(id).getSalary();
        return new Money(salary.getAmount().divide(BigDecimal.valueOf(2080), 2, RoundingMode.HALF_UP),
                salary.getCurrency());
    }

    /** Count of currently-employed people, for cross-module capacity/utilization math. */
    @Transactional(readOnly = true)
    public long activeHeadcount() {
        return employees.countByTerminationDateIsNull();
    }
}
