package com.simpleerp.hr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.simpleerp.hr.dto.LeaveRequestRequest;
import com.simpleerp.shared.InvalidStateException;
import com.simpleerp.shared.ValidationException;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for the leave workflow: validation, transitions, and the business-day count. */
@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @Mock private LeaveRequestRepository requests;
    @Mock private EmployeeService employees;

    private LeaveRequestService service;
    private Employee employee;

    @BeforeEach
    void setUp() {
        service = new LeaveRequestService(requests, employees);
        employee = new Employee();
        employee.setName("Alice Chen");
    }

    @Test
    void submitRejectsEndBeforeStart() {
        var request = new LeaveRequestRequest(1L, LeaveType.VACATION,
                LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 18));

        assertThatThrownBy(() -> service.submit(request)).isInstanceOf(ValidationException.class);
    }

    @Test
    void submitRejectsOverlapWithApprovedLeave() {
        when(employees.require(1L)).thenReturn(employee);
        when(requests.countApprovedOverlapping(any(), any(), any())).thenReturn(1L);

        var request = new LeaveRequestRequest(1L, LeaveType.VACATION,
                LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 24));

        assertThatThrownBy(() -> service.submit(request)).isInstanceOf(ValidationException.class);
    }

    @Test
    void submitCreatesPendingRequest() {
        when(employees.require(1L)).thenReturn(employee);
        when(requests.countApprovedOverlapping(any(), any(), any())).thenReturn(0L);
        when(requests.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new LeaveRequestRequest(1L, LeaveType.VACATION,
                LocalDate.of(2026, 6, 22), LocalDate.of(2026, 6, 26)); // Mon–Fri = 5 business days
        var response = service.submit(request);

        assertThat(response.status()).isEqualTo(LeaveStatus.PENDING);
        assertThat(response.businessDays()).isEqualTo(5);
    }

    @Test
    void businessDaysExcludeWeekends() {
        when(employees.require(1L)).thenReturn(employee);
        when(requests.countApprovedOverlapping(any(), any(), any())).thenReturn(0L);
        when(requests.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Fri 2026-06-19 through Mon 2026-06-22 spans a weekend → 2 business days (Fri, Mon).
        var request = new LeaveRequestRequest(1L, LeaveType.SICK,
                LocalDate.of(2026, 6, 19), LocalDate.of(2026, 6, 22));

        assertThat(service.submit(request).businessDays()).isEqualTo(2);
    }

    @Test
    void approveMovesPendingToApproved() {
        LeaveRequest pending = pending();
        when(requests.findById(1L)).thenReturn(Optional.of(pending));
        when(requests.countApprovedOverlapping(any(), any(), any())).thenReturn(0L);

        var response = service.approve(1L, "Dana HR");

        assertThat(response.status()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(response.reviewer()).isEqualTo("Dana HR");
        assertThat(pending.getDecidedAt()).isNotNull();
    }

    @Test
    void approveRejectsNonPending() {
        LeaveRequest leave = pending();
        leave.setStatus(LeaveStatus.APPROVED);
        when(requests.findById(1L)).thenReturn(Optional.of(leave));

        assertThatThrownBy(() -> service.approve(1L, "HR")).isInstanceOf(InvalidStateException.class);
    }

    @Test
    void cancelRejectsLeaveThatHasAlreadyStarted() {
        LeaveRequest leave = pending();
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setStartDate(LocalDate.now().minusDays(1));
        leave.setEndDate(LocalDate.now().plusDays(2));
        when(requests.findById(1L)).thenReturn(Optional.of(leave));

        assertThatThrownBy(() -> service.cancel(1L)).isInstanceOf(InvalidStateException.class);
    }

    @Test
    void cancelAllowsFutureApprovedLeave() {
        LeaveRequest leave = pending();
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setStartDate(LocalDate.now().plusDays(5));
        leave.setEndDate(LocalDate.now().plusDays(9));
        when(requests.findById(1L)).thenReturn(Optional.of(leave));

        assertThat(service.cancel(1L).status()).isEqualTo(LeaveStatus.CANCELLED);
    }

    private LeaveRequest pending() {
        Employee owner = new Employee();
        owner.setName("Bob Martinez");
        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(owner);
        leave.setType(LeaveType.VACATION);
        leave.setStartDate(LocalDate.of(2026, 6, 22));
        leave.setEndDate(LocalDate.of(2026, 6, 26));
        leave.setStatus(LeaveStatus.PENDING);
        return leave;
    }
}
