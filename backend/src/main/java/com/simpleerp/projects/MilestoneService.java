package com.simpleerp.projects;

import com.simpleerp.projects.dto.MilestoneRequest;
import com.simpleerp.projects.dto.MilestoneResponse;
import com.simpleerp.shared.NotFoundException;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Milestone management: create, complete, and waive. Resolved milestones unblock completion. */
@Service
@Transactional
public class MilestoneService {

    private final MilestoneRepository milestones;
    private final ProjectService projects;

    public MilestoneService(MilestoneRepository milestones, ProjectService projects) {
        this.milestones = milestones;
        this.projects = projects;
    }

    /** Adds a milestone to a project. */
    public MilestoneResponse create(MilestoneRequest request) {
        Project project = projects.load(request.projectId());
        Milestone milestone = new Milestone();
        milestone.setProject(project);
        milestone.setName(request.name());
        milestone.setDueDate(request.dueDate());
        return MilestoneResponse.from(milestones.save(milestone));
    }

    /** Marks a milestone completed (no-op timestamp refresh if already completed). */
    public MilestoneResponse complete(Long id) {
        Milestone milestone = load(id);
        if (milestone.getCompletedAt() == null) {
            milestone.setCompletedAt(Instant.now());
        }
        return MilestoneResponse.from(milestone);
    }

    /** Waives a milestone so the project can complete without it. */
    public MilestoneResponse waive(Long id) {
        Milestone milestone = load(id);
        milestone.setWaived(true);
        return MilestoneResponse.from(milestone);
    }

    /** Milestones on one project. */
    @Transactional(readOnly = true)
    public List<MilestoneResponse> listByProject(Long projectId) {
        return milestones.findByProject_Id(projectId).stream().map(MilestoneResponse::from).toList();
    }

    private Milestone load(Long id) {
        return milestones.findById(id).orElseThrow(() -> new NotFoundException("Milestone", id));
    }
}
