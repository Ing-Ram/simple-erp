package com.simpleerp.hr.dto;

import com.simpleerp.hr.Department;

/** Department representation returned to clients. */
public record DepartmentResponse(Long id, String name, Long managerId, String managerName) {

    /** Maps an entity to its response shape; the single home for this mapping. */
    public static DepartmentResponse from(Department d) {
        return new DepartmentResponse(
                d.getId(),
                d.getName(),
                d.getManager() == null ? null : d.getManager().getId(),
                d.getManager() == null ? null : d.getManager().getName());
    }
}
