package dev.shoangenes.file;

import dev.shoangenes.config.StorageProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;
import dev.shoangenes.utils.LoggerUtil;

/**
 * Manages file operations such as saving, deleting, and retrieving files
 * in a specified storage directory. Ensures thread-safe access to files
 * using a FileAccessManager.
 */
public class FileManager {
    /*================================ Fields =================================*/

    private final FileAccessManager fileAccessManager;
    private final Path storageDir;
    private final StorageProperties storageProperties;
    private final Logger logger = LoggerUtil.getLogger(FileManager.class);

    /*=========================== Constructor ================================*/

    public FileManager() {
        this.storageProperties = StorageProperties.getInstance();
        this.storageDir = storageProperties.getFileStoragePath();
        this.fileAccessManager = FileAccessManager.getInstance();
    }

    /*=========================== Public Methods =============================*/

    /**
     * Saves a file to the storage directory.
     *
     * @param filename The name of the file to save.
     * @param data The byte array representing the file content.
     * @throws IOException If an I/O error occurs during file operations.
     * @throws IllegalArgumentException If the filename is invalid or the data exceeds size limits.
     */
    public void saveFile(String filename, byte[] data) throws IOException {
        logger.info("Attempting to save file: " + filename);
        validateFileName(filename);
        validateFileContent(data);

        Path filePath = getFilePath(filename);
        fileAccessManager.acquireWriteLock(filename);
        try {
            if (!Files.exists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
            }

            Files.write(filePath, data);
            logger.info("File saved successfully: " + filename);
        } catch (IOException e) {
            logger.severe("Failed to save file: " + filename + " - " + e.getMessage());
            throw e;
        } finally {
            fileAccessManager.releaseWriteLock(filename);
        }
    }

    /**
     * Deletes a file from the storage directory and returns its content as a byte array.
     * Returns an empty Optional if the file does not exist.
     *
     * @param filename The name of the file to delete.
     * @return An Optional containing the file content if it existed, otherwise empty.
     * @throws IOException If an I/O error occurs during file operations.
     * @throws IllegalArgumentException If the filename is invalid.
     */
    public Optional<byte[]> deleteFile(String filename) throws  IOException {
        logger.info("Attempting to delete file: " + filename);
        validateFileName(filename);

        Path filePath = getFilePath(filename);
        fileAccessManager.acquireWriteLock(filename);
        try {
            if (Files.exists(filePath)) {
                Optional<byte[]> fileData = Optional.of(Files.readAllBytes(filePath));
                Files.delete(filePath);
                logger.info("File deleted successfully: " + filename);
                return fileData;
            }
            logger.warning("File not found for deletion: " + filename);
            return Optional.empty();
        } catch (IOException e) {
            logger.severe("Failed to delete file: " + filename + " - " + e.getMessage());
            throw e;
        } finally {
            fileAccessManager.releaseWriteLock(filename);
        }
    }

    /**
     * Retrieves a file's content as a byte array without deleting it.
     * Returns an empty Optional if the file does not exist.
     *
     * @param filename The name of the file to retrieve.
     * @return An Optional containing the file content if it exists, otherwise empty.
     * @throws IOException If an I/O error occurs during file operations.
     * @throws IllegalArgumentException If the filename is invalid.
     */
    public Optional<byte[]> getFile(String filename) throws IOException {
        logger.info("Attempting to retrieve file: " + filename);
        validateFileName(filename);

        Path filePath = getFilePath(filename);
        fileAccessManager.acquireReadLock(filename);
        try {
            if (Files.exists(filePath)) {
                logger.info("File retrieved successfully: " + filename);
                return Optional.of(Files.readAllBytes(filePath));
            }
            logger.warning("File not found for retrieval: " + filename);
            return Optional.empty();
        } catch (IOException e) {
            logger.severe("Failed to retrieve file: " + filename + " - " + e.getMessage());
            throw e;
        } finally {
            fileAccessManager.releaseReadLock(filename);
        }
    }

    /*=========================== Private Methods ============================*/

    /** Constructs the full file path for a given filename.
     *
     * @param filename The name of the file.
     * @return The full Path object representing the file location.
     */
    private Path getFilePath(String filename) {
        return storageDir.resolve(filename);
    }

    /**
     * Validates the filename to ensure it is not null or empty.
     * @param filename The filename to validate.
     * @throws IllegalArgumentException if the filename is invalid.
     */
    private void validateFileName(String filename) {
        if (filename == null || filename.isEmpty()) {
            logger.warning("Invalid filename: " + filename);
            throw new IllegalArgumentException("Invalid filename: " + filename);
        }
    }

    /**
     * Validates the file content to ensure it is not null and does not exceed size limits.
     * @param data The byte array representing the file content.
     * @throws IllegalArgumentException if the data is null or exceeds size limits.
     */
    private void validateFileContent(byte[] data) {
        if (data == null) {
            logger.warning("Data cannot be null");
            throw new IllegalArgumentException("Data cannot be null");
        }

        if (data.length > storageProperties.getMaxFileSize()) {
            logger.warning("File size exceeds maximum limit: " + data.length + " bytes");
            throw new IllegalArgumentException("File size exceeds maximum limit");
        }
    }
}