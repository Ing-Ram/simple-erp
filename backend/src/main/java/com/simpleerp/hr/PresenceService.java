package com.simpleerp.hr;

import com.simpleerp.hr.dto.CheckInRequest;
import com.simpleerp.hr.dto.PresenceResponse;
import com.simpleerp.hr.dto.RollCallEntry;
import com.simpleerp.hr.dto.RollCallResponse;
import com.simpleerp.shared.InvalidStateException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Building check-in / check-out and the emergency roll-call that accounts for every active employee. */
@Service
@Transactional
public class PresenceService {

    private final BuildingPresenceRepository presence;
    private final EmployeeRepository employees;
    private final EmployeeService employeeService;
    private final LeaveRequestRepository leave;

    public PresenceService(BuildingPresenceRepository presence, EmployeeRepository employees,
                           EmployeeService employeeService, LeaveRequestRepository leave) {
        this.presence = presence;
        this.employees = employees;
        this.employeeService = employeeService;
        this.leave = leave;
    }

    /** Checks an employee in for the day, on site or remote. */
    public PresenceResponse checkIn(CheckInRequest request) {
        Employee employee = employeeService.require(request.employeeId());
        BuildingPresence record = new BuildingPresence();
        record.setEmployee(employee);
        record.setWorkMode(request.workMode());
        record.setCheckInAt(Instant.now());
        return PresenceResponse.from(presence.save(record));
    }

    /** Closes the employee's most recent open check-in; fails if they are not currently checked in. */
    public PresenceResponse checkOut(Long employeeId) {
        BuildingPresence record = presence
                .findFirstByEmployee_IdAndCheckOutAtIsNullOrderByCheckInAtDesc(employeeId)
                .orElseThrow(() -> new InvalidStateException("Employee " + employeeId + " is not checked in"));
        record.setCheckOutAt(Instant.now());
        return PresenceResponse.from(record);
    }

    /**
     * Closes every still-open check-in, flagging each as auto-checked-out. Run at end of day so a
     * forgotten check-in doesn't linger as PRESENT into the next day; returns how many were closed.
     */
    public int autoCheckOutOpen() {
        Instant now = Instant.now();
        List<BuildingPresence> open = presence.findByCheckOutAtIsNull();
        for (BuildingPresence record : open) {
            record.setCheckOutAt(now);
            record.setAutoCheckedOut(true);
        }
        return open.size();
    }

    /**
     * Reconciles every active employee into an accountability status as of now. Presence wins over
     * excused absence so anyone physically on site is never overlooked.
     */
    @Transactional(readOnly = true)
    public RollCallResponse rollCall() {
        Instant now = Instant.now();
        Instant startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();

        Map<Long, BuildingPresence> latestByEmployee = new HashMap<>();
        for (BuildingPresence p : presence.findByCheckInAtGreaterThanEqual(startOfToday)) {
            latestByEmployee.merge(p.getEmployee().getId(), p,
                    (a, b) -> a.getCheckInAt().isAfter(b.getCheckInAt()) ? a : b);
        }
        Set<Long> onLeave = Set.copyOf(leave.employeeIdsOnLeave(LocalDate.now()));

        List<RollCallEntry> entries = employees.findAll().stream()
                .filter(e -> e.getTerminationDate() == null)
                .map(e -> toEntry(e, latestByEmployee.get(e.getId()), onLeave.contains(e.getId())))
                .sorted(Comparator.comparing((RollCallEntry r) -> r.status().ordinal())
                        .thenComparing(RollCallEntry::name))
                .toList();

        return new RollCallResponse(
                now,
                count(entries, AccountabilityStatus.PRESENT),
                count(entries, AccountabilityStatus.CHECKED_OUT),
                count(entries, AccountabilityStatus.REMOTE),
                count(entries, AccountabilityStatus.ON_LEAVE),
                count(entries, AccountabilityStatus.UNACCOUNTED),
                entries);
    }

    /** Classifies one employee from their latest check-in today and whether they are on leave. */
    private RollCallEntry toEntry(Employee e, BuildingPresence p, boolean onLeave) {
        String department = e.getDepartment() == null ? null : e.getDepartment().getName();
        AccountabilityStatus status;
        Instant since;
        if (p != null && p.getWorkMode() == WorkMode.ON_SITE && p.getCheckOutAt() == null) {
            status = AccountabilityStatus.PRESENT;
            since = p.getCheckInAt();
        } else if (p != null && p.getWorkMode() == WorkMode.REMOTE) {
            status = AccountabilityStatus.REMOTE;
            since = p.getCheckInAt();
        } else if (p != null && p.getWorkMode() == WorkMode.ON_SITE) {
            status = AccountabilityStatus.CHECKED_OUT;
            since = p.getCheckOutAt();
        } else if (onLeave) {
            status = AccountabilityStatus.ON_LEAVE;
            since = null;
        } else {
            status = AccountabilityStatus.UNACCOUNTED;
            since = null;
        }
        return new RollCallEntry(e.getId(), e.getName(), department, status, since);
    }

    private long count(List<RollCallEntry> entries, AccountabilityStatus status) {
        return entries.stream().filter(r -> r.status() == status).count();
    }
}
