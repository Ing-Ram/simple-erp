package com.simpleerp.hr;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for building presence (check-in / check-out) records. */
public interface BuildingPresenceRepository extends JpaRepository<BuildingPresence, Long> {

    /** All check-ins from {@code since} onward — the day's records for the roll-call. */
    List<BuildingPresence> findByCheckInAtGreaterThanEqual(Instant since);

    /** The employee's most recent still-open check-in, if any — what a check-out closes. */
    Optional<BuildingPresence> findFirstByEmployee_IdAndCheckOutAtIsNullOrderByCheckInAtDesc(Long employeeId);
}
