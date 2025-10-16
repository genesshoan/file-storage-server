package dev.shoangenes.util;

import dev.shoangenes.config.StorageProperties;
import dev.shoangenes.utils.FileValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for FileValidator.
 * Tests are organized by validation method for clarity.
 */
class FileValidatorTest {

    @BeforeAll
    static void setup() {
        // Ensure StorageProperties is initialized for tests
        StorageProperties.getInstance();
    }

    @Nested
    class ValidateFileNameTests {

        @Test
        void validName_shouldNotThrow() {
            assertThatCode(() -> FileValidator.validateFileName("file_123.txt"))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(strings = {"file.txt", "document_2024.pdf", "photo-001.jpg", "data123.bin"})
        void validNames_shouldNotThrow(String filename) {
            assertThatCode(() -> FileValidator.validateFileName(filename))
                    .doesNotThrowAnyException();
        }

        @Test
        void nullOrEmpty_shouldThrow() {
            assertThatThrownBy(() -> FileValidator.validateFileName(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid filename");

            assertThatThrownBy(() -> FileValidator.validateFileName(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid filename");
        }

        @ParameterizedTest
        @ValueSource(strings = {"../secret.txt", "folder/file.txt", "folder\\file.txt", "..\\etc\\passwd"})
        void pathTraversal_shouldThrow(String filename) {
            assertThatThrownBy(() -> FileValidator.validateFileName(filename))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("path separators");
        }

        @ParameterizedTest
        @ValueSource(strings = {"file@name.txt", "file#123.txt", "file$test.txt", "file<name>.txt", "file|name.txt"})
        void invalidCharacters_shouldThrow(String filename) {
            assertThatThrownBy(() -> FileValidator.validateFileName(filename))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("invalid characters");
        }

        @Test
        void spacesInFilename_shouldThrow() {
            assertThatThrownBy(() -> FileValidator.validateFileName("file name.txt"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("invalid characters");
        }

        @Test
        void tooLong_shouldThrow() {
            int maxLen = StorageProperties.getInstance().getMaxFileNameLength();
            String longName = "a".repeat(maxLen + 1);

            assertThatThrownBy(() -> FileValidator.validateFileName(longName))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("too long");
        }

        @Test
        void maxLengthAllowed_shouldNotThrow() {
            int maxLen = StorageProperties.getInstance().getMaxFileNameLength();
            String maxName = "a".repeat(maxLen);

            assertThatCode(() -> FileValidator.validateFileName(maxName))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    class ValidateFileIdTests {

        @ParameterizedTest
        @ValueSource(strings = {"1", "123", "999", "2147483647"})
        void validIds_shouldNotThrow(String id) {
            assertThatCode(() -> FileValidator.validateFileId(id))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-1", "-999"})
        void notPositive_shouldThrow(String id) {
            assertThatThrownBy(() -> FileValidator.validateFileId(id))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive integer");
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "12.5", "1e10", "null", ""})
        void notNumeric_shouldThrow(String id) {
            assertThatThrownBy(() -> FileValidator.validateFileId(id))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("valid integer");
        }

        @Test
        void overflow_shouldThrow() {
            String overflow = String.valueOf(Long.MAX_VALUE) + "0";

            assertThatThrownBy(() -> FileValidator.validateFileId(overflow))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("valid integer");
        }
    }

    @Nested
    class ValidateFileSizeTests {

        @Test
        void zeroSize_shouldNotThrow() {
            assertThatCode(() -> FileValidator.validateFileSize(0))
                    .doesNotThrowAnyException();
        }

        @Test
        void validSize_shouldNotThrow() {
            long max = StorageProperties.getInstance().getMaxFileSize();
            assertThatCode(() -> FileValidator.validateFileSize(max / 2))
                    .doesNotThrowAnyException();
        }

        @Test
        void maxSizeAllowed_shouldNotThrow() {
            long max = StorageProperties.getInstance().getMaxFileSize();
            assertThatCode(() -> FileValidator.validateFileSize(max))
                    .doesNotThrowAnyException();
        }

        @Test
        void negative_shouldThrow() {
            assertThatThrownBy(() -> FileValidator.validateFileSize(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be negative");
        }

        @Test
        void exceedsLimit_shouldThrow() {
            long max = StorageProperties.getInstance().getMaxFileSize();

            assertThatThrownBy(() -> FileValidator.validateFileSize(max + 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("exceeds maximum limit");
        }
    }

    @Nested
    class ValidateFileDataTests {

        @Test
        void validData_shouldNotThrow() {
            byte[] data = new byte[10];
            assertThatCode(() -> FileValidator.validateFileData(data, 10))
                    .doesNotThrowAnyException();
        }

        @Test
        void emptyData_shouldNotThrow() {
            byte[] data = new byte[0];
            assertThatCode(() -> FileValidator.validateFileData(data, 0))
                    .doesNotThrowAnyException();
        }

        @Test
        void nullData_shouldThrow() {
            assertThatThrownBy(() -> FileValidator.validateFileData(null, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        void sizeMismatch_shouldThrow() {
            byte[] data = new byte[3];

            assertThatThrownBy(() -> FileValidator.validateFileData(data, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not match expected size");
        }

        @Test
        void largeDataWithCorrectSize_shouldNotThrow() {
            long largeSize = 1_000_000; // 1MB
            byte[] data = new byte[(int) largeSize];

            assertThatCode(() -> FileValidator.validateFileData(data, largeSize))
                    .doesNotThrowAnyException();
        }
    }
}