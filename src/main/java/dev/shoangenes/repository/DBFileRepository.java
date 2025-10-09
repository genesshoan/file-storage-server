package dev.shoangenes.repository;

import dev.shoangenes.exception.DatabaseException;
import dev.shoangenes.model.FileMetadata;
import org.jetbrains.annotations.Nullable;
import java.sql.*;

/**
 * Database-backed implementation of the IFileRepository interface.
 * This class manages file ID and name mappings using SQL database.
 */
public class DBFileRepository implements IFileRepository, AutoCloseable {
    /*============================== Fields ============================*/

    private final Connection connection;

    /*=========================== Constructors =========================*/

    public DBFileRepository() throws DatabaseException {
        try {
            connection = DatabaseManager.getInstance().getConnection();
            createTableIfNotExists();
        } catch (SQLException e) {
            throw new DatabaseException("Error initializing database connection", e);
        }
    }

    /*=========================== Public Methods ========================*/

    /**
     * Generates the next available unique ID for a file.
     * @return the next unique ID or 1 if the table is empty.
     * @throws DatabaseException if a database access error occurs.
     */
    public int generateNextId() throws DatabaseException {
        String query = "SELECT MAX(id) + 1 AS max_id FROM files";
        try (Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(query)) {
                if (rs.next()) {
                    int nextId = rs.getInt("max_id");
                    return nextId > 0 ? nextId : 1; // Ensure ID starts from 1
                } else {
                    return 1; // Start IDs from 1 if table is empty
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error generating next ID", e);
        }
    }

    /**
     * Saves a mapping between an ID and a file name.
     * @param id the file ID.
     * @param fileName the file name.
     * @throws DatabaseException if a database access error occurs.
     * @throws IllegalArgumentException if fileName is null.
     */
    public void saveMapping(int id, String fileName) throws DatabaseException {
        if (fileName == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }

        String query = "INSERT INTO files (id, file_name) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, fileName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error saving mapping", e);
        }
    }

    /**
     * Removes the mapping for a given ID.
     * @param id the ID to remove.
     * @return the removed FileMetadata, or null if not found.
     * @throws DatabaseException if a database access error occurs.
     */
    @Nullable
    public FileMetadata removeMapping(int id) throws DatabaseException {
        String getMetadataQuery = "SELECT * from files WHERE id = ?";
        String deleteQuery = "DELETE FROM files WHERE id = ?";
        FileMetadata fileMetadata = null;

        try (PreparedStatement pstmt = connection.prepareStatement(getMetadataQuery)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    fileMetadata = new FileMetadata(rs.getInt("id"), rs.getString("file_name"));
                }

                if (fileMetadata != null) {
                    try (PreparedStatement deletePstmt = connection.prepareStatement(deleteQuery)) {
                        deletePstmt.setInt(1, id);
                        deletePstmt.executeUpdate();
                    }
                }
                return fileMetadata; // Return null if not found
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error removing mapping", e);
        }
    }

    /**
     * Returns the file name associated with a given ID.
     * @param id the file ID.
     * @return the file name, or null if not found.
     * @throws DatabaseException if a database access error occurs.
     */
    @Nullable
    public String getFileName(int id) throws DatabaseException {
        String query = "SELECT file_name FROM files WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("file_name");
                } else {
                    return null; // Not found
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting file name", e);
        }
    }

    /**
     * Returns the ID associated with the given file name.
     * @param fileName the file name.
     * @return the file ID, or -1 if not found.
     * @throws DatabaseException if a database access error occurs.
     */
    public int getId(String fileName) throws DatabaseException {
        if (fileName == null) {
            return -1;
        }

        String query = "SELECT id FROM files WHERE file_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, fileName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    return -1; // Not found
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting file ID", e);
        }
    }

    /**
     * Checks if a file with the given ID exists.
     * @param id the file ID.
     * @return true if the file exists, false otherwise.
     * @throws DatabaseException if a database access error occurs.
     */
    public boolean fileExists(int id) throws DatabaseException {
        String query = "SELECT 1 FROM files WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking if file exists by ID", e);
        }
    }

    /**
     * Checks if a file with the given name exists.
     * @param fileName the file name.
     * @return true if the file exists, false otherwise.
     * @throws DatabaseException if a database access error occurs.
     */
    public boolean fileExists(String fileName) throws DatabaseException {
        if (fileName == null) {
            return false;
        }

        String query = "SELECT 1 FROM files WHERE file_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, fileName);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking if file exists by name", e);
        }
    }

    /**
     * Closes the database connection.
     * @throws Exception if an error occurs while closing the connection.
     */
    @Override
    public void close() throws DatabaseException {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error closing database connection", e);
        }
    }

    //=========================== Private Methods =======================//

    /**
     * Creates the file's table if it does not already exist.
     * @throws DatabaseException if a database access error occurs.
     */
    private void createTableIfNotExists() throws DatabaseException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS files (" +
                "id INT PRIMARY KEY," +
                "file_name VARCHAR(255) UNIQUE NOT NULL" +
                ")";
        try (Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            throw new DatabaseException("Error creating table", e);
        }
    }
}
