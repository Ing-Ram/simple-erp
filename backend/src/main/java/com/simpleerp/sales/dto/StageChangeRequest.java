package com.simpleerp.sales.dto;

import com.simpleerp.sales.OpportunityStage;
import jakarta.validation.constraints.NotNull;

/** Client payload for advancing an opportunity to a later open stage. */
public record StageChangeRequest(@NotNull OpportunityStage stage) {
}
