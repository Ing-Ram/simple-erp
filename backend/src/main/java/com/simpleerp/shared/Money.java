package com.simpleerp.shared;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Embeddable monetary value: a BigDecimal amount plus an ISO-4217 currency code.
 *
 * <p>Money is never represented as a {@code double}. v1 is single-currency (USD), but the
 * currency code travels with every amount so multi-currency later is a data change, not a
 * refactor. Arithmetic rejects mixed currencies rather than silently converting.
 */
@Embeddable
public class Money {

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency;

    /** Required by JPA; not for application use. */
    protected Money() {
    }

    /** Creates a money value from an amount and a currency code. */
    public Money(BigDecimal amount, String currency) {
        this.amount = Objects.requireNonNull(amount, "amount");
        this.currency = Objects.requireNonNull(currency, "currency");
    }

    /** A zero amount in the given currency. */
    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    /** Returns the sum of this and the given money; currencies must match. */
    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    /** Returns this minus the given money; currencies must match. */
    public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    /** Returns this amount scaled by the given factor, keeping the currency. */
    public Money times(BigDecimal factor) {
        return new Money(amount.multiply(factor), currency);
    }

    /** True when the amount is zero or below. */
    public boolean isZeroOrNegative() {
        return amount.signum() <= 0;
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + currency + " vs " + other.currency);
        }
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    /** Value equality compares amount numerically (ignoring scale) and currency. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money other)) {
            return false;
        }
        return amount.compareTo(other.amount) == 0 && currency.equals(other.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }
}
