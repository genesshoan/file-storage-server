package dev.shoangenes.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for FSHeader enum.
 */
public class FSHeaderTest {
    @Test
    void fromCode_validCodes_shouldReturnCorrectEnum() {
        // Test all valid codes
        assertThat(FSHeader.fromCode(1)).isEqualTo(FSHeader.FILE_NAME);
        assertThat(FSHeader.fromCode(2)).isEqualTo(FSHeader.ID);
        assertThat(FSHeader.fromCode(3)).isEqualTo(FSHeader.MESSAGE);
    }

    @Test
    void fromCode_invalidCode_shouldThrow() {
        // Test invalid code throws exception
        assertThatThrownBy(() -> FSHeader.fromCode(99))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid FSHeader code");
        assertThatThrownBy(() -> FSHeader.fromCode(0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid FSHeader code");
    }

    @Test
    void getCode_shouldReturnCorrectCode() {
        // Test that getCode returns the expected code for each enum
        assertThat(FSHeader.FILE_NAME.getCode()).isEqualTo(1);
        assertThat(FSHeader.ID.getCode()).isEqualTo(2);
        assertThat(FSHeader.MESSAGE.getCode()).isEqualTo(3);
    }
}
