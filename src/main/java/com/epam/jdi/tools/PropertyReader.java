package com.epam.jdi.tools;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.func.JAction1;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class PropertyReader {
    private static String propertiesPath;
    private static volatile Properties properties;
    private static InputStream inputStream;

    private PropertyReader() {
    }
    private static String getCorrectPath() {
        if (propertiesPath.charAt(0) != '/')
            propertiesPath = "/" + propertiesPath;
        return propertiesPath;
    }

    public static Properties readProperties() {
        properties = new Properties();
        try {
            inputStream = PropertyReader.class.getResourceAsStream(getCorrectPath());
            if (inputStream != null)
                properties.load(inputStream);
        } catch (Exception ex) {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return properties;
    }

    private static Properties loadProperties() {
        return properties != null ? properties : readProperties();
    }

    public static Properties getProperties(String path) {
        propertiesPath = path;
        return readProperties();
    }


    public static String getProperty(String propertyName) throws IOException {
        return loadProperties().getProperty(propertyName);
    }

    public static void fillAction(JAction1<String> action, String name) {
        String prop = null;
        try {
            prop = getProperty(name);
        } catch (Exception ignore) {}
        if (isNotBlank(prop))
            action.execute(prop);
    }

}