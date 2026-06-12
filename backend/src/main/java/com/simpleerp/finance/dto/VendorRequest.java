package com.simpleerp.finance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/** Client payload for creating a vendor. */
public record VendorRequest(
        @NotBlank String name,
        @Email String email,
        @PositiveOrZero int paymentTermsDays) {
}
