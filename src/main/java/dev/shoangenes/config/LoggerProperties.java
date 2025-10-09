package dev.shoangenes.config;

import java.util.Properties;
import java.util.logging.Level;

public class LoggerProperties {
    /*============================= Singleton Instance ========================*/

    private static LoggerProperties instance;

    /*============================= Default Values ============================*/

    private static final String CONFIG_FILE = "logger.properties";

    private String level = "INFO";
    private String format = "[%d] [%l] %m";
    private String destination = "both"; // "console", "file", or "both"
    private String filePath = "logs/server.log";

    /*============================= Constructors ==============================*/

    /**
     * Private constructor to prevent instantiation.
     */
    private LoggerProperties() {
        Properties props = PropertiesLoader.load(CONFIG_FILE);
        if (props == null) {
            return;
        }
        this.level = props.getProperty("logger.level", level);
        this.format = props.getProperty("logger.format", format);
        this.destination = props.getProperty("logger.destination", destination);
        this.filePath = props.getProperty("logger.file", filePath);
    }

    /*============================= Public Methods =============================*/

    /**
     * Get the singleton instance of LoggerProperties.
     *
     * @return LoggerProperties instance
     */
    public static LoggerProperties getInstance() {
        if (instance == null) {
            instance = new LoggerProperties();
        }
        return instance;
    }

    /*============================= Getters =============================*/

    public Level getLevel() {
        return Level.parse(level);
    }

    public String getFormat() {
        return format;
    }

    public String getDestination() {
        return destination;
    }

    public String getFilePath() {
        return filePath;
    }
}