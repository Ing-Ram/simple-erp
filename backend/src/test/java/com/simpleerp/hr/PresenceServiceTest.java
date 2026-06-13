package com.simpleerp.hr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.simpleerp.shared.InvalidStateException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for check-out, which has the branch logic; roll-call is covered by the smoke test. */
@ExtendWith(MockitoExtension.class)
class PresenceServiceTest {

    @Mock private BuildingPresenceRepository presence;
    @Mock private EmployeeRepository employees;
    @Mock private EmployeeService employeeService;
    @Mock private LeaveRequestRepository leave;

    private PresenceService service;

    @BeforeEach
    void setUp() {
        service = new PresenceService(presence, employees, employeeService, leave);
    }

    @Test
    void checkOutClosesTheOpenRecord() {
        Employee employee = new Employee();
        employee.setName("Alice Chen");
        BuildingPresence open = new BuildingPresence();
        open.setEmployee(employee);
        open.setWorkMode(WorkMode.ON_SITE);
        open.setCheckInAt(Instant.now().minusSeconds(3600));
        when(presence.findFirstByEmployee_IdAndCheckOutAtIsNullOrderByCheckInAtDesc(1L))
                .thenReturn(Optional.of(open));

        var response = service.checkOut(1L);

        assertThat(open.getCheckOutAt()).isNotNull();
        assertThat(response.checkOutAt()).isNotNull();
    }

    @Test
    void checkOutFailsWhenNotCheckedIn() {
        when(presence.findFirstByEmployee_IdAndCheckOutAtIsNullOrderByCheckInAtDesc(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.checkOut(1L)).isInstanceOf(InvalidStateException.class);
    }
}
