package dev.shoangenes.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.shoangenes.config.StorageProperties;
import dev.shoangenes.utils.LoggerUtil;
import java.sql.Connection;
import java.util.logging.Logger;

/**
 * Singleton class to manage database connections using HikariCP connection pooling.
 */
class DatabaseManager implements AutoCloseable {
    /*============================= Singleton Instance ========================*/

    private static DatabaseManager instance = null;

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

        dataSource = new HikariDataSource(hikariConfig);
    }

    /*============================= Public Methods =============================*/

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Get a connection from the HikariCP connection pool.
     * @return A Connection object
     * @throws Exception if unable to get a connection
     */
    public Connection getConnection() throws Exception {
        return dataSource.getConnection();
    }

    /**
     * Close the HikariCP data source.
     */
    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Closing Hikari DataSource");
        }
    }
}
