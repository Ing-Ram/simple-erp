package com.simpleerp.projects;

import com.simpleerp.projects.dto.MilestoneRequest;
import com.simpleerp.projects.dto.MilestoneResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for milestones. */
@RestController
@RequestMapping("/api/v1/projects/milestones")
public class MilestoneController {

    private final MilestoneService service;

    public MilestoneController(MilestoneService service) {
        this.service = service;
    }

    /** Lists milestones on a project. */
    @GetMapping
    public List<MilestoneResponse> list(@RequestParam Long projectId) {
        return service.listByProject(projectId);
    }

    /** Adds a milestone and returns 201 with its location. */
    @PostMapping
    public ResponseEntity<MilestoneResponse> create(@Valid @RequestBody MilestoneRequest request) {
        MilestoneResponse milestone = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/projects/milestones/" + milestone.id())).body(milestone);
    }

    /** Marks the milestone completed. */
    @PostMapping("/{id}/complete")
    public MilestoneResponse complete(@PathVariable Long id) {
        return service.complete(id);
    }

    /** Waives the milestone so the project can complete without it. */
    @PostMapping("/{id}/waive")
    public MilestoneResponse waive(@PathVariable Long id) {
        return service.waive(id);
    }
}
