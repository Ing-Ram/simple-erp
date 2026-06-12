package com.simpleerp.hr;

import com.simpleerp.hr.dto.DepartmentRequest;
import com.simpleerp.hr.dto.DepartmentResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for departments. */
@RestController
@RequestMapping("/api/v1/hr/departments")
public class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    /** Lists all departments. */
    @GetMapping
    public List<DepartmentResponse> list() {
        return service.list();
    }

    /** Creates a department and returns 201 with its location. */
    @PostMapping
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse department = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/hr/departments/" + department.id()))
                .body(department);
    }
}
