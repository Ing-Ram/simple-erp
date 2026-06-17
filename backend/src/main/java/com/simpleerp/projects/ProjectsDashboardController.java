package com.simpleerp.projects;

import com.simpleerp.projects.dto.ProjectsDashboardResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only Projects dashboard endpoint: the whole summary in one call. */
@RestController
@RequestMapping("/api/v1/projects/dashboard")
public class ProjectsDashboardController {

    private final ProjectsDashboardService service;

    public ProjectsDashboardController(ProjectsDashboardService service) {
        this.service = service;
    }

    /** Returns the Projects dashboard summary as of today. */
    @GetMapping
    public ProjectsDashboardResponse dashboard() {
        return service.summary();
    }
}
