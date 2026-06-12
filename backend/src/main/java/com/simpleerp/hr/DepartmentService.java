package com.simpleerp.hr;

import com.simpleerp.hr.dto.DepartmentRequest;
import com.simpleerp.hr.dto.DepartmentResponse;
import com.simpleerp.shared.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Department management. */
@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departments;
    private final EmployeeRepository employees;

    public DepartmentService(DepartmentRepository departments, EmployeeRepository employees) {
        this.departments = departments;
        this.employees = employees;
    }

    /** Creates a department, optionally assigning an existing employee as its manager. */
    public DepartmentResponse create(DepartmentRequest request) {
        Department department = new Department();
        department.setName(request.name());
        if (request.managerId() != null) {
            Employee manager = employees.findById(request.managerId())
                    .orElseThrow(() -> new NotFoundException("Employee", request.managerId()));
            department.setManager(manager);
        }
        return DepartmentResponse.from(departments.save(department));
    }

    /** All departments. */
    @Transactional(readOnly = true)
    public List<DepartmentResponse> list() {
        return departments.findAll().stream().map(DepartmentResponse::from).toList();
    }

    /** Loads one department as a response, or throws 404. */
    @Transactional(readOnly = true)
    public DepartmentResponse get(Long id) {
        return DepartmentResponse.from(load(id));
    }

    /** Loads the department entity or throws 404; for internal use within a transaction. */
    private Department load(Long id) {
        return departments.findById(id).orElseThrow(() -> new NotFoundException("Department", id));
    }
}
