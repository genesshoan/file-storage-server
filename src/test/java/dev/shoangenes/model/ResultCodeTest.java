package dev.shoangenes.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ResultCode enum.
 * Tests cover both valid and invalid code lookups.
 */
public class ResultCodeTest {
    @Test
    void testFromCode_validCodes() {
        assertThat(ResultCode.fromCode(200)).isEqualTo(ResultCode.SUCCESS);
        assertThat(ResultCode.fromCode(400)).isEqualTo(ResultCode.BAD_REQUEST);
        assertThat(ResultCode.fromCode(403)).isEqualTo(ResultCode.FORBIDDEN);
        assertThat(ResultCode.fromCode(404)).isEqualTo(ResultCode.NOT_FOUND);
        assertThat(ResultCode.fromCode(500)).isEqualTo(ResultCode.SERVER_ERROR);
    }

    @Test
    void testFromCode_invalidCode() {
        assertThatThrownBy(() -> ResultCode.fromCode(999))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No matching result code for code: 999");
    }
}