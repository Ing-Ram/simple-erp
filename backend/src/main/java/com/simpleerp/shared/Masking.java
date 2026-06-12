package com.simpleerp.shared;

/** Helpers for masking sensitive identifiers before they are stored or returned. */
public final class Masking {

    private static final char MASK_CHAR = '•'; // bullet •

    private Masking() {
    }

    /**
     * Masks all but the last {@code visible} characters of a value, replacing each hidden
     * character with a bullet. Returns null for null, and leaves short values (length at or
     * below {@code visible}) untouched since there is nothing to hide.
     *
     * <p>Example: {@code maskExceptLast("000123456", 3)} → {@code "••••••456"}.
     */
    public static String maskExceptLast(String value, int visible) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        if (trimmed.length() <= visible) {
            return trimmed;
        }
        int hidden = trimmed.length() - visible;
        return String.valueOf(MASK_CHAR).repeat(hidden) + trimmed.substring(hidden);
    }
}
