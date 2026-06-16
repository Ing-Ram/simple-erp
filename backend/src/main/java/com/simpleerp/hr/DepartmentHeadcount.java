package com.simpleerp.hr;

/** Active-employee count for one department, produced by a repository aggregation. */
public record DepartmentHeadcount(String department, long headcount) {
}
