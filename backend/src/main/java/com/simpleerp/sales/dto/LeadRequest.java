package com.simpleerp.sales.dto;

import com.simpleerp.sales.LeadSource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Client payload for capturing a lead. */
public record LeadRequest(
        @NotBlank String name,
        String company,
        @Email String email,
        @NotNull LeadSource source) {
}
