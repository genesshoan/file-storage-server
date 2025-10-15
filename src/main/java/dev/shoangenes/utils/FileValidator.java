package dev.shoangenes.utils;

import dev.shoangenes.config.StorageProperties;

public class FileValidator {
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
        if (filename.length() > 255) { // TODO: Make configurable
            throw new IllegalArgumentException("Filename is too long: " + filename);
        }
    }

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

    public static void validateFileSize(long size) {
        if (size < 0) {
            throw new IllegalArgumentException("File size cannot be negative: " + size);
        }
        if (size > StorageProperties.getInstance().getMaxFileSize()) {
            throw new IllegalArgumentException("File size exceeds maximum limit: " + size);
        }
    }

    public static void validateFileData(byte[] data, long expectedSize) {
        if (data == null) {
            throw new IllegalArgumentException("File data cannot be null");
        }
        if (data.length != expectedSize) {
            throw new IllegalArgumentException("File data size does not match expected size: " + expectedSize);
        }
    }
}
