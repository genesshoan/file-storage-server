package dev.shoangenes.config;

import java.util.Properties;

/**
 * Singleton class to manage server-related configuration properties.
 */
public class ServerProperties {
    /*============================= Singleton Instance ========================*/
    private static ServerProperties instance = null;

    /*============================= Default Values ============================*/

    private static final String CONFIG_FILE = "server.properties";

    private int port = 8080;
    private String host = "localhost";
    private int threadPoolSize = 10;
    private int backlog = 50;

    /*============================= Constructors ==============================*/

    /**
     * Private constructor to enforce the singleton pattern.
     * Loads properties from the configuration file.
     */
    private ServerProperties() {
        Properties props = PropertiesLoader.load(CONFIG_FILE);
        if (props == null) {
            return;
        }
        this.port = Integer.parseInt(props.getProperty("server.port"));
        this.host = props.getProperty("server.host");
        this.threadPoolSize = Integer.parseInt(props.getProperty("server.threadPoolSize"));
        this.backlog = Integer.parseInt(props.getProperty("server.backlog"));
    }

    /*============================= Getters ================================*/

    /**
     * Singleton instance accessor.
     * @return The singleton instance of ServerProperties.
     */
    public static ServerProperties getInstance() {
        if (instance == null) {
            instance = new ServerProperties();
        }
        return instance;
    }

    /**
     * Get the server port.
     * @return The port number the server listens on.
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the server host.
     * @return The host address the server binds to.
     */
    public String getHost() {
        return host;
    }

    /**
     * Get the thread pool size.
     * @return The number of threads in the server's thread pool.
     */
    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    /**
     * Get the server backlog size.
     * @return The maximum number of pending connections.
     */
    public int getBacklog() {
        return backlog;
    }
}
