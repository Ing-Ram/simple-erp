package com.simpleerp.hr.dto;

import jakarta.validation.constraints.NotBlank;

/** Client payload for creating a department. The manager can be assigned later. */
public record DepartmentRequest(@NotBlank String name, Long managerId) {
}
