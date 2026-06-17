package com.simpleerp.projects;

import com.simpleerp.projects.dto.ProjectRequest;
import com.simpleerp.projects.dto.ProjectResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for projects and their lifecycle. */
@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    /** Lists all projects with derived budget figures. */
    @GetMapping
    public List<ProjectResponse> list() {
        return service.list();
    }

    /** Returns one project. */
    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    /** Creates a project and returns 201 with its location. */
    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
        ProjectResponse project = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + project.id())).body(project);
    }

    /** Activates a planned or on-hold project. */
    @PostMapping("/{id}/activate")
    public ProjectResponse activate(@PathVariable Long id) {
        return service.activate(id);
    }

    /** Pauses an active project. */
    @PostMapping("/{id}/hold")
    public ProjectResponse hold(@PathVariable Long id) {
        return service.putOnHold(id);
    }

    /** Completes a project; requires every milestone completed or waived. */
    @PostMapping("/{id}/complete")
    public ProjectResponse complete(@PathVariable Long id) {
        return service.complete(id);
    }

    /** Cancels a project. */
    @PostMapping("/{id}/cancel")
    public ProjectResponse cancel(@PathVariable Long id) {
        return service.cancel(id);
    }
}
