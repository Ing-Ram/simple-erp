package com.simpleerp.hr;

import com.simpleerp.hr.dto.LeaveRequestRequest;
import com.simpleerp.hr.dto.LeaveRequestResponse;
import com.simpleerp.shared.InvalidStateException;
import com.simpleerp.shared.NotFoundException;
import com.simpleerp.shared.ValidationException;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The leave workflow: submission with validation, and approve / reject / cancel transitions. */
@Service
@Transactional
public class LeaveRequestService {

    private final LeaveRequestRepository requests;
    private final EmployeeService employees;

    public LeaveRequestService(LeaveRequestRepository requests, EmployeeService employees) {
        this.requests = requests;
        this.employees = employees;
    }

    /**
     * Submits a PENDING leave request after validating the date range and that it does not overlap
     * the employee's existing approved leave.
     */
    public LeaveRequestResponse submit(LeaveRequestRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new ValidationException("End date must not be before start date");
        }
        Employee employee = employees.require(request.employeeId());
        if (requests.countApprovedOverlapping(employee.getId(), request.startDate(), request.endDate()) > 0) {
            throw new ValidationException("Leave overlaps existing approved leave for this employee");
        }
        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(employee);
        leave.setType(request.type());
        leave.setStartDate(request.startDate());
        leave.setEndDate(request.endDate());
        leave.setStatus(LeaveStatus.PENDING);
        return LeaveRequestResponse.from(requests.save(leave));
    }

    /** Approves a pending request; rejects the transition from any other state. */
    public LeaveRequestResponse approve(Long id, String reviewer) {
        LeaveRequest leave = load(id);
        switch (leave.getStatus()) {
            case PENDING -> {
                if (requests.countApprovedOverlapping(
                        leave.getEmployee().getId(), leave.getStartDate(), leave.getEndDate()) > 0) {
                    throw new ValidationException("Leave overlaps existing approved leave for this employee");
                }
                decide(leave, LeaveStatus.APPROVED, reviewer);
            }
            case APPROVED, REJECTED, CANCELLED ->
                    throw new InvalidStateException("Cannot approve leave in status " + leave.getStatus());
        }
        return LeaveRequestResponse.from(leave);
    }

    /** Rejects a pending request; rejects the transition from any other state. */
    public LeaveRequestResponse reject(Long id, String reviewer) {
        LeaveRequest leave = load(id);
        switch (leave.getStatus()) {
            case PENDING -> decide(leave, LeaveStatus.REJECTED, reviewer);
            case APPROVED, REJECTED, CANCELLED ->
                    throw new InvalidStateException("Cannot reject leave in status " + leave.getStatus());
        }
        return LeaveRequestResponse.from(leave);
    }

    /** Cancels approved leave, but only while it is still in the future. */
    public LeaveRequestResponse cancel(Long id) {
        LeaveRequest leave = load(id);
        switch (leave.getStatus()) {
            case APPROVED -> {
                if (!leave.getStartDate().isAfter(LocalDate.now())) {
                    throw new InvalidStateException("Can only cancel leave that starts in the future");
                }
                leave.setStatus(LeaveStatus.CANCELLED);
            }
            case PENDING, REJECTED, CANCELLED ->
                    throw new InvalidStateException("Cannot cancel leave in status " + leave.getStatus());
        }
        return LeaveRequestResponse.from(leave);
    }

    /** Records a decision: sets status, reviewer (defaulting to "HR"), and the decided timestamp. */
    private void decide(LeaveRequest leave, LeaveStatus status, String reviewer) {
        leave.setStatus(status);
        leave.setReviewer(reviewer == null || reviewer.isBlank() ? "HR" : reviewer);
        leave.setDecidedAt(Instant.now());
    }

    private LeaveRequest load(Long id) {
        return requests.findById(id).orElseThrow(() -> new NotFoundException("Leave request", id));
    }
}
