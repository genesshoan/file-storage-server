package dev.shoangenes.config;

import org.jetbrains.annotations.Nullable;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Utility class for loading properties from a file.
 */
public class PropertiesLoader {
    /**
     * Loads properties from a file in the classpath.
     *
     * @param filePath The path to the properties file.
     * @return A Properties object containing the loaded properties, or null if loading fails.
     */
    @Nullable
    static Properties load(String filePath) {
        try (InputStream input = PropertiesLoader.class.getClassLoader().getResourceAsStream(filePath)) {
            if (input == null) {
                return null;
            }
            Properties prop = new Properties();
            prop.load(input);
            return prop;
        } catch (IOException ex) {
            return null;
        }
    }
}
