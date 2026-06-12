package com.simpleerp.hr.dto;

/** Optional payload for approving or rejecting leave; carries who made the decision. */
public record LeaveDecisionRequest(String reviewer) {
}
