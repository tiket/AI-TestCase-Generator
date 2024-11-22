package util;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConfigReader {

    private static Properties properties = new Properties();
    private static final List<String> PROPERTIES_FILE_PATHS = new ArrayList<>();

    static {
        // Add all properties file paths to the list
        PROPERTIES_FILE_PATHS.add("src/main/resources/credential.properties");
        PROPERTIES_FILE_PATHS.add("src/main/resources/config.properties");
        // Add more file paths as needed

        // Load properties from each file
        for (String filePath : PROPERTIES_FILE_PATHS) {
            try (InputStream input = new FileInputStream(filePath)) {
                properties.load(input);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static String getValue(String keyName) {
        return properties.getProperty(keyName);
    }

}


