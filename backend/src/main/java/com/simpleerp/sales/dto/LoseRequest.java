package com.simpleerp.sales.dto;

import jakarta.validation.constraints.NotBlank;

/** Client payload for marking an opportunity lost; a reason is required. */
public record LoseRequest(@NotBlank String lostReason) {
}
