package dev.shoangenes.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for OpCode enum.
 * Tests cover both valid and invalid code lookups.
 */
public class OpCodeTest {
    @Test
    void testFromCode_validCodes() {
        assertThat(OpCode.fromCode(1)).isEqualTo(OpCode.PUT);
        assertThat(OpCode.fromCode(2)).isEqualTo(OpCode.GET);
        assertThat(OpCode.fromCode(3)).isEqualTo(OpCode.DELETE);
    }

    @Test
    void testFromCode_invalidCode() {
        assertThatThrownBy(() -> OpCode.fromCode(99))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No matching operation code for code: 99");
    }
}
