package dev.shoangenes.repository;

import dev.shoangenes.exception.DatabaseException;
import dev.shoangenes.model.FileMetadata;
import dev.shoangenes.utils.LoggerUtil;
import org.jetbrains.annotations.Nullable;
import java.sql.*;
import java.util.logging.Logger;

/**
 * Database-backed implementation of the IFileRepository interface.
 * This class manages file ID and name mappings using SQL database.
 */
public class DBFileRepository implements IFileRepository {
    /*============================== Fields ============================*/

    private final Connection connection;

    /*=========================== Logger ===========================*/

    private final Logger logger = LoggerUtil.getLogger(DBFileRepository.class);

    /*=========================== Constructors =========================*/

    public DBFileRepository() throws DatabaseException {
        try {
            connection = DatabaseManager.getInstance().getConnection();
            createTableIfNotExists();
            logger.info("Database connection initialized and table checked/created.");
        } catch (SQLException e) {
            logger.severe("Error initializing database connection: " + e.getMessage());
            throw new DatabaseException("Error initializing database connection", e);
        }
    }

    /*=========================== Public Methods ========================*/

    /**
     * Saves a new file mapping and returns the generated ID.
     * @param fileName the name of the file to save.
     * @return the generated file ID.
     * @throws DatabaseException if a database access error occurs.
     */
    public int saveMapping(String fileName) throws DatabaseException {
        if (fileName == null) {
            logger.warning("Attempted to save a mapping with a null file name.");
            throw new IllegalArgumentException("File name cannot be null");
        }

        int generatedId = -1;

        String query = "INSERT INTO files (file_name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, fileName);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                    logger.info("File mapping saved. File name: " + fileName + ", Generated ID: " + generatedId);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error saving mapping for file name '" + fileName + "': " + e.getMessage());
            throw new DatabaseException("Error saving mapping", e);
        }
        return generatedId;
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
                        logger.info("File mapping removed. ID: " + id + ", File name: " + fileMetadata.getName());
                    }
                } else {
                    logger.warning("No file mapping found to remove for ID: " + id);
                }
                return fileMetadata; // Return null if not found
            }
        } catch (SQLException e) {
            logger.severe("Error removing mapping for ID '" + id + "': " + e.getMessage());
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
                    String fileName = rs.getString("file_name");
                    logger.info("File name found for ID " + id + ": " + fileName);
                    return fileName;
                } else {
                    logger.warning("No file name found for ID: " + id);
                    return null; // Not found
                }
            }
        } catch (SQLException e) {
            logger.severe("Error getting file name for ID '" + id + "': " + e.getMessage());
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
            logger.warning("Attempted to get ID for a null file name.");
            return -1;
        }

        String query = "SELECT id FROM files WHERE file_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, fileName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    logger.info("ID found for file name '" + fileName + "': " + id);
                    return id;
                } else {
                    logger.warning("No ID found for file name: " + fileName);
                    return -1; // Not found
                }
            }
        } catch (SQLException e) {
            logger.severe("Error getting file ID for file name '" + fileName + "': " + e.getMessage());
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
                boolean exists = rs.next();
                logger.info("File existence check by ID " + id + ": " + exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.severe("Error checking if file exists by ID '" + id + "': " + e.getMessage());
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
            logger.warning("Attempted to check existence for a null file name.");
            return false;
        }

        String query = "SELECT 1 FROM files WHERE file_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, fileName);
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean exists = rs.next();
                logger.info("File existence check by name '" + fileName + "': " + exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.severe("Error checking if file exists by name '" + fileName + "': " + e.getMessage());
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
                logger.info("Database connection closed successfully.");
            }
        } catch (SQLException e) {
            logger.severe("Error closing database connection: " + e.getMessage());
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
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "file_name VARCHAR(255) UNIQUE NOT NULL" +
                ")";
        try (Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
            logger.info("Checked/created table 'files' in the database.");
        } catch (SQLException e) {
            logger.severe("Error creating table: " + e.getMessage());
            throw new DatabaseException("Error creating table", e);
        }
    }
}
