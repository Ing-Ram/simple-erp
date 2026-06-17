package com.simpleerp.projects;

import com.simpleerp.projects.dto.TimeEntryRequest;
import com.simpleerp.projects.dto.TimeEntryResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for time entries. */
@RestController
@RequestMapping("/api/v1/projects/time-entries")
public class TimeEntryController {

    private final TimeEntryService service;

    public TimeEntryController(TimeEntryService service) {
        this.service = service;
    }

    /** Lists time entries on a task. */
    @GetMapping
    public List<TimeEntryResponse> list(@RequestParam Long taskId) {
        return service.listByTask(taskId);
    }

    /** Logs time against a task and returns 201 with its location. */
    @PostMapping
    public ResponseEntity<TimeEntryResponse> log(@Valid @RequestBody TimeEntryRequest request) {
        TimeEntryResponse entry = service.log(request);
        return ResponseEntity.created(URI.create("/api/v1/projects/time-entries/" + entry.id())).body(entry);
    }
}
