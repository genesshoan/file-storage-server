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

    private static final String CONFIG_FILE = "storage.properties";

    // Storage settings
    private String repositoryType = "database"; // "file" or "database"
    private long maxFileSize = 10485760; // 10MB default

    // File storage settings
    private String fileStoragePath = "data/files";

    // Database storage settings
    private String databaseUrl = "jdbc:sqlite:data/database.db";
    private String databaseDriver = "org.sqlite.JDBC";
    private boolean requireAuthentication = false;
    private String password = null;
    private String username = null;
    private int maxConnections = 1;
    private int maxFileNameLength = 255;

    /*============================= Constructors ==============================*/

    private StorageProperties() {
        Properties props = PropertiesLoader.load(CONFIG_FILE);
        if (props == null) {
            return;
        }

        this.repositoryType = props.getProperty("storage.repositoryType");
        this.fileStoragePath = props.getProperty("storage.fileStoragePath");
        this.databaseUrl = props.getProperty("storage.databaseUrl");
        this.databaseDriver = props.getProperty("storage.databaseDriver");
        this.maxFileSize = Long.parseLong(props.getProperty("storage.maxFileSize"));
        this.requireAuthentication = Boolean.parseBoolean(props.getProperty("storage.requireAuthentication"));
        this.requireAuthentication = Boolean.parseBoolean(props.getProperty("storage.requireAuthentication"));
        this.username = props.getProperty("storage.username");
        this.password = props.getProperty("storage.password");
        this.maxConnections = Integer.parseInt(props.getProperty("storage.maxConnections"));
        this.maxFileNameLength = Integer.parseInt(props.getProperty("storage.maxFileNameLength", "255"));

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

    /**
     * Get the maximum allowed length for file names.
     * @return Maximum file name length
     */
    public int getMaxFileNameLength() {
        return 255; // Typical maximum filename length for many filesystems
    }
}
