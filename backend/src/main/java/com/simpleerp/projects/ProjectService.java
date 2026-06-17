package com.simpleerp.projects;

import com.simpleerp.finance.CustomerService;
import com.simpleerp.hr.EmployeeService;
import com.simpleerp.projects.dto.ProjectRequest;
import com.simpleerp.projects.dto.ProjectResponse;
import com.simpleerp.shared.InvalidStateException;
import com.simpleerp.shared.Money;
import com.simpleerp.shared.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Project lifecycle and budget reporting. Manager (HR) and optional customer (Finance) are validated
 * and named through their services. Completing a project requires every milestone resolved.
 */
@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projects;
    private final MilestoneRepository milestones;
    private final ProjectCostService cost;
    private final CustomerService customers;
    private final EmployeeService employees;

    public ProjectService(ProjectRepository projects, MilestoneRepository milestones,
                          ProjectCostService cost, CustomerService customers, EmployeeService employees) {
        this.projects = projects;
        this.milestones = milestones;
        this.cost = cost;
        this.customers = customers;
        this.employees = employees;
    }

    /** Creates a PLANNED project. */
    public ProjectResponse create(ProjectRequest request) {
        employees.get(request.managerEmployeeId());           // 404 if the manager doesn't exist
        if (request.customerId() != null) {
            customers.get(request.customerId());
        }
        Project project = new Project();
        project.setName(request.name());
        project.setCustomerId(request.customerId());
        project.setManagerEmployeeId(request.managerEmployeeId());
        project.setStartDate(request.startDate());
        project.setTargetEndDate(request.targetEndDate());
        project.setBudget(new Money(request.budget(), request.currency()));
        project.setStatus(ProjectStatus.PLANNED);
        return toResponse(projects.save(project));
    }

    /** Moves a planned or on-hold project into ACTIVE. */
    public ProjectResponse activate(Long id) {
        Project project = load(id);
        switch (project.getStatus()) {
            case PLANNED, ON_HOLD -> project.setStatus(ProjectStatus.ACTIVE);
            case ACTIVE, COMPLETED, CANCELLED ->
                    throw new InvalidStateException("Cannot activate a project in status " + project.getStatus());
        }
        return toResponse(project);
    }

    /** Pauses an active project. */
    public ProjectResponse putOnHold(Long id) {
        Project project = load(id);
        switch (project.getStatus()) {
            case ACTIVE -> project.setStatus(ProjectStatus.ON_HOLD);
            case PLANNED, ON_HOLD, COMPLETED, CANCELLED ->
                    throw new InvalidStateException("Cannot hold a project in status " + project.getStatus());
        }
        return toResponse(project);
    }

    /**
     * Completes a project — only once every milestone is completed or waived, so the conversation
     * about unfinished checkpoints happens explicitly.
     */
    public ProjectResponse complete(Long id) {
        Project project = load(id);
        switch (project.getStatus()) {
            case ACTIVE, ON_HOLD -> {
                long unresolved = milestones.countByProject_IdAndCompletedAtIsNullAndWaivedFalse(id);
                if (unresolved > 0) {
                    throw new InvalidStateException(
                            unresolved + " milestone(s) are neither completed nor waived");
                }
                project.setStatus(ProjectStatus.COMPLETED);
            }
            case PLANNED, COMPLETED, CANCELLED ->
                    throw new InvalidStateException("Cannot complete a project in status " + project.getStatus());
        }
        return toResponse(project);
    }

    /** Cancels a project that has not already reached a terminal state. */
    public ProjectResponse cancel(Long id) {
        Project project = load(id);
        switch (project.getStatus()) {
            case PLANNED, ACTIVE, ON_HOLD -> project.setStatus(ProjectStatus.CANCELLED);
            case COMPLETED, CANCELLED ->
                    throw new InvalidStateException("Cannot cancel a project in status " + project.getStatus());
        }
        return toResponse(project);
    }

    /** All projects, with derived budget figures. */
    @Transactional(readOnly = true)
    public List<ProjectResponse> list() {
        return projects.findAll().stream().map(this::toResponse).toList();
    }

    /** Loads one project as a response, or throws 404. */
    @Transactional(readOnly = true)
    public ProjectResponse get(Long id) {
        return toResponse(load(id));
    }

    /** Loads the project entity or throws 404; for internal/cross-service use within a transaction. */
    public Project load(Long id) {
        return projects.findById(id).orElseThrow(() -> new NotFoundException("Project", id));
    }

    /** Resolves cross-module names and derived spend, then maps to the response. */
    private ProjectResponse toResponse(Project p) {
        String customerName = p.getCustomerId() == null ? null : customers.get(p.getCustomerId()).getName();
        String managerName = employees.get(p.getManagerEmployeeId()).name();
        Money spent = cost.spent(p.getId(), p.getBudget().getCurrency());
        return ProjectResponse.from(p, customerName, managerName, spent.getAmount(),
                cost.percentConsumed(p, spent));
    }
}
