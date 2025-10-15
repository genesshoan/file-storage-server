package dev.shoangenes.repository;

import dev.shoangenes.exception.DatabaseException;
import dev.shoangenes.model.FileMetadata;

/**
 * Interface for file repository operations.
 *
 * Implementations can be file-based or database-backed.
 */
public interface IFileRepository extends AutoCloseable {

    /**
     * Saves a new file mapping and returns the generated ID.
     * @param fileName the name of the file to save.
     * @return the generated file ID.
     * @throws DatabaseException if a database access error occurs.
     */
    int saveMapping(String fileName) throws DatabaseException;

    /**
     * Removes the mapping for a given ID.
     * @param id the ID to remove.
     * @throws DatabaseException if a database access error occurs.
     */
    FileMetadata removeMapping(int id) throws DatabaseException;

    /**
     * Returns the file name associated with a given ID.
     * @param id the file ID.
     * @return the file name, or null if not found.
     * @throws DatabaseException if a database access error occurs.
     */
    String getFileName(int id) throws DatabaseException;

    /**
     * Returns the ID associated with the given file name.
     * @param fileName the file name.
     * @return the file ID, or -1 if not found.
     * @throws DatabaseException if a database access error occurs.
     */
    int getId(String fileName) throws DatabaseException;

    /**
     * Checks if a file with the given ID exists.
     * @param id the file ID.
     * @return true if the file exists, false otherwise.
     * @throws DatabaseException if a database access error occurs.
     */
    boolean fileExists(int id) throws DatabaseException;

    /**
     * Checks if a file with the given name exists.
     * @param fileName the file name.
     * @return true if the file exists, false otherwise.
     * @throws DatabaseException if a database access error occurs.
     */
    boolean fileExists(String fileName) throws DatabaseException;
}
