package dev.shoangenes.utils;

import dev.shoangenes.config.StorageProperties;

public class FileValidator {
    /**
     * Validates a filename to ensure it does not contain path traversal characters
     * or invalid characters, and checks its length against configured limits.
     * @param filename the filename to validate
     * @throws IllegalArgumentException if the filename is invalid
     */
    public static void validateFileName(String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Invalid filename: " + filename);
        }
        if (filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            throw new IllegalArgumentException("Filename cannot contain path separators or '..': " + filename);
        }
        if (!filename.matches("^[a-zA-Z0-9._-]+$")) {
            throw new IllegalArgumentException("Filename contains invalid characters: " + filename);
        }
        if (filename.length() > StorageProperties.getInstance().getMaxFileNameLength()) {
            throw new IllegalArgumentException("Filename is too long: " + filename);
        }
    }

    /**
     * Validates a file ID to ensure it is a positive integer.
     * @param fileId the file ID to validate
     * @throws IllegalArgumentException if the file ID is invalid
     */
    public static void validateFileId(String fileId) {
        try {
            int id = Integer.parseInt(fileId);
            if (id < 1) {
                throw new IllegalArgumentException("File ID must be a positive integer: " + fileId);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("File ID must be a valid integer: " + fileId);
        }
    }

    /**
     * Validates a file size to ensure it is non-negative and does not exceed
     * configured maximum limits.
     * @param size the file size to validate
     * @throws IllegalArgumentException if the file size is invalid
     */
    public static void validateFileSize(long size) {
        if (size < 0) {
            throw new IllegalArgumentException("File size cannot be negative: " + size);
        }
        if (size > StorageProperties.getInstance().getMaxFileSize()) {
            throw new IllegalArgumentException("File size exceeds maximum limit: " + size);
        }
    }

    /**
     * Validates that the provided file data is not null and matches the expected size.
     * @param data the file data to validate
     * @param expectedSize the expected size of the file data
     * @throws IllegalArgumentException if the file data is null or does not match the expected size
     */
    public static void validateFileData(byte[] data, long expectedSize) {
        if (data == null) {
            throw new IllegalArgumentException("File data cannot be null");
        }
        if (data.length != expectedSize) {
            throw new IllegalArgumentException("File data size does not match expected size: " + expectedSize);
        }
    }
}
