package dev.shoangenes.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.shoangenes.config.StorageProperties;
import dev.shoangenes.utils.LoggerUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Singleton class to manage database connections using HikariCP connection pooling.
 */
class DatabaseManager implements AutoCloseable {
    /*============================= Singleton Instance ========================*/

    private static final DatabaseManager instance = new DatabaseManager();

    /*================================= Fields ================================*/

    HikariConfig hikariConfig;
    HikariDataSource dataSource;

    /*============================= Logger ===============================*/

    Logger logger = LoggerUtil.getLogger(DatabaseManager.class);

    /*============================= Constructors ==============================*/

    /**
     * Private constructor to prevent instantiation.
     */
    private DatabaseManager() {
        StorageProperties props = StorageProperties.getInstance();

        hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(props.getDatabaseUrl());
        hikariConfig.setDriverClassName(props.getDatabaseDriver());
        hikariConfig.setMaximumPoolSize(props.getMaxConnections());
        if (props.isRequireAuthentication()) {
            hikariConfig.setUsername(props.getDatabaseUsername());
            hikariConfig.setPassword(props.getDatabasePassword());
        }

        try {
            dataSource = new HikariDataSource(hikariConfig);
            logger.info("Hikari DataSource initialized successfully.");
        } catch (Exception e) {
            logger.severe("Failed to initialize Hikari DataSource: " + e.getMessage());
            throw e;
        }
    }

    /*============================= Public Methods =============================*/

    public static DatabaseManager getInstance() {
        return instance;
    }

    /**
     * Get a connection from the HikariCP connection pool.
     * @return A Connection object
     * @throws Exception if unable to get a connection
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection conn = dataSource.getConnection();
            logger.fine("Database connection acquired from pool.");
            return conn;
        } catch (SQLException e) {
            logger.severe("Failed to acquire database connection: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Close the HikariCP data source.
     */
    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Closing Hikari DataSource");
        } else {
            logger.warning("Attempted to close Hikari DataSource, but it was already closed or null.");
        }
    }
}
