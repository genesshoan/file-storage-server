package dev.shoangenes.utils;

import dev.shoangenes.config.LoggerProperties;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

/**
 * Utility class for configuring and retrieving loggers.
 * This class sets up logging based on properties defined in LoggerProperties.
 */
public class LoggerUtil {
    /*============================= Logger Configuration ========================*/

    private static FileHandler fileHandler;
    private static Level level;
    private static boolean consoleEnabled;
    private static boolean isConfigured = false;

    /**
     * Static initializer to configure the logger based on LoggerProperties.
     */
    static {
        try {
            LoggerProperties props = LoggerProperties.getInstance();
            level = props.getLevel();
            consoleEnabled = props.getDestination().equalsIgnoreCase("console")
                    || props.getDestination().equalsIgnoreCase("both");

            File logFile = new File(props.getFilePath());
            File logDir = logFile.getParentFile();
            if (logDir != null && !logDir.exists()) {
                logDir.mkdirs();
            }

            fileHandler = new FileHandler(props.getFilePath(), true);
            fileHandler.setFormatter(new SimpleFormatter());
        } catch (IOException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }

    /*============================= Public Methods =============================*/

    /**
     * Retrieves a logger for the specified class, configuring it if necessary.
     *
     * @param clazz The class for which the logger is requested.
     * @return A configured Logger instance.
     */
    public static Logger getLogger(Class<?> clazz) {
        Logger logger = Logger.getLogger(clazz.getName());
        if (!isConfigured) {
            logger.setLevel(level);
            logger.setUseParentHandlers(false);

            logger.addHandler(fileHandler);

            if (consoleEnabled) {
                ConsoleHandler consoleHandler = new ConsoleHandler();
                consoleHandler.setLevel(level);
                logger.addHandler(consoleHandler);
            }
            isConfigured = true;
        }
        return logger;
    }
}
