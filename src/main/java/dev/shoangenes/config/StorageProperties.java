package dev.shoangenes.config;

import org.jetbrains.annotations.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Singleton class to manage storage-related configuration properties.
 * Supports both file-based and database storage options.
 */
public class StorageProperties {
    /*============================= Singleton Instance ========================*/

    private static StorageProperties instance = null;

    /*============================= Default Values ============================*/

    private static final String CONFIG_FILE = "storage.properties.example";

    // Storage settings
    private String repositoryType; // "file" or "database"
    private long maxFileSize;

    // File storage settings
    private String fileStoragePath;

    // Database storage settings
    private String databaseUrl;
    private String databaseDriver;
    private boolean requireAuthentication;
    private String password;
    private String username;
    private int maxConnections;

    /*============================= Constructors ==============================*/

    private StorageProperties() {
        Properties props = PropertiesLoader.load(CONFIG_FILE);
        if (props == null) {
            return;
        }

        this.repositoryType = props.getProperty("storage.repositoryType", "database");
        this.fileStoragePath = props.getProperty("storage.fileStoragePath", "data/files");
        this.databaseUrl = props.getProperty("storage.databaseUrl", "jdbc:sqlite:data/database.db");
        this.databaseDriver = props.getProperty("storage.databaseDriver", "org.sqlite.JDBC");

        try {
            this.maxFileSize = Long.parseLong(props.getProperty("storage.maxFileSize", "10485760"));
        } catch (NumberFormatException e) {
            this.maxFileSize = 10485760; // Default to 10MB
        }

        try {
            this.requireAuthentication = Boolean.parseBoolean(props.getProperty("storage.requireAuthentication", "false"));
        } catch (Exception e) {
            this.requireAuthentication = false;
        }

        this.requireAuthentication = Boolean.parseBoolean(props.getProperty("storage.requireAuthentication", "false"));
        this.username = props.getProperty("storage.username", null);
        this.password = props.getProperty("storage.password", null);
        try {
            Integer.parseInt(props.getProperty("storage.maxConnections", "1"));
        } catch (NumberFormatException e) {
            this.maxConnections = 1;
        }
    }

    /*============================= Public Methods =============================*/

    public static StorageProperties getInstance() {
        if (instance == null) {
            instance = new StorageProperties();
        }
        return instance;
    }

    /*============================= Getters =============================*/

    /**
     * Get the type of repository used for file storage.
     * @return "file" or "database"
     */
    public String getRepositoryType() {
        return repositoryType;
    }

    /**
     * Get the file storage path.
     * @return Path to file storage.
     */
    public Path getFileStoragePath() {
        return Paths.get(fileStoragePath);
    }

    /**
     * Get the database URL if using database storage.
     * @return Database URL or null if using file-based storage
     */
    @Nullable
    public String getDatabaseUrl() {
        return repositoryType.equals("file") ? null : databaseUrl;
    }

    /**
     * Get the database driver if using database storage.
     * @return Database driver or null if using file-based storage
     */
    @Nullable
    public String getDatabaseDriver() {
        return repositoryType.equals("file") ? null : databaseDriver;
    }

    /**
     * Get the maximum allowed file size for uploads.
     * @return Maximum file size in bytes
     */
    public long getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * Check if authentication is required for file uploads.
     * @return true if authentication is required, false otherwise
     */
    public boolean isRequireAuthentication() {
        return requireAuthentication;
    }

    /**
     * Get the database username if using database storage.
     * @return Database username or null if using file-based storage
     */
    @Nullable
    public String getDatabaseUsername() {
        return repositoryType.equals("file") ? null : username;
    }

    /**
     * Get the database password if using database storage.
     * @return Database password or null if using file-based storage
     */
    @Nullable
    public String getDatabasePassword() {
        return repositoryType.equals("file") ? null : password;
    }

    /**
     * Get the maximum number of database connections in the pool.
     * @return Maximum number of connections
     */
    public int getMaxConnections() {
        return maxConnections;
    }
}
