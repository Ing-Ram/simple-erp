package com.simpleerp.hr;

import com.simpleerp.hr.dto.EmployeeRequest;
import com.simpleerp.hr.dto.EmployeeResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for employees. */
@RestController
@RequestMapping("/api/v1/hr/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    /** Lists all employees, with ON_LEAVE derived for anyone on approved leave today. */
    @GetMapping
    public List<EmployeeResponse> list() {
        return service.list();
    }

    /** Returns one employee. */
    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    /** Hires an employee and returns 201 with its location. */
    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse employee = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/hr/employees/" + employee.id()))
                .body(employee);
    }

    /** Terminates an employee effective on the given date (defaults to today). */
    @PostMapping("/{id}/terminate")
    public EmployeeResponse terminate(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDate) {
        return service.terminate(id, effectiveDate == null ? LocalDate.now() : effectiveDate);
    }
}
