package dev.shoangenes.utils;

import dev.shoangenes.config.LoggerProperties;
import dev.shoangenes.config.PropertiesLoader;

import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Level;

public class LogFilter implements Filter {
    /*============================= Fields ==============================*/

    private final Level minLevel;

    /*============================= Constructors ==============================*/

    /**
     * Constructs a LogFilter with the minimum log level from ServerProperties.
     */
    public LogFilter() {
        this.minLevel = LoggerProperties.getInstance().getLevel();
    }

    /*============================= Methods =============================*/

    /**
     * Checks if a LogRecord should be logged based on its level.
     * @param record  a LogRecord
     * @return true if the record should be logged, false otherwise
     */
    @Override
    public boolean isLoggable(LogRecord record) {
        if (record == null || record.getLevel() == null) {
            return false;
        }
        return record.getLevel().intValue() >= minLevel.intValue();
    }
}