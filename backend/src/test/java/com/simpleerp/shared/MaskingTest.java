package com.simpleerp.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for the masking helper. */
class MaskingTest {

    @Test
    void masksAllButTheLastThree() {
        assertThat(Masking.maskExceptLast("000123456", 3)).isEqualTo("••••••456");
    }

    @Test
    void leavesNullUntouched() {
        assertThat(Masking.maskExceptLast(null, 3)).isNull();
    }

    @Test
    void doesNotMaskWhenAtOrBelowVisibleLength() {
        assertThat(Masking.maskExceptLast("12", 3)).isEqualTo("12");
        assertThat(Masking.maskExceptLast("789", 3)).isEqualTo("789");
    }

    @Test
    void trimsSurroundingWhitespaceBeforeMasking() {
        assertThat(Masking.maskExceptLast("  123456  ", 3)).isEqualTo("•••456");
    }
}
