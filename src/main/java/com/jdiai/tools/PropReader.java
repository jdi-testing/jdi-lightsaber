package com.jdiai.tools;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Consumer;

import static com.jdiai.tools.StringUtils.LINE_BREAK;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class PropReader {
    private String propertiesPath;
    private volatile Properties properties;
    private InputStream inputStream;

    public PropReader(String path) {
        propertiesPath = path;
    }
    public String getPath() {
        if (propertiesPath.charAt(0) != '/') {
            propertiesPath = "/" + propertiesPath;
        }
        return propertiesPath;
    }

    public Properties readProperties() {
        properties = new Properties();
        try {
            inputStream = PropReader.class.getResourceAsStream(getPath());
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (Exception ex) {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return properties;
    }

    public Properties loadProperties() {
        return properties != null ? properties : readProperties();
    }

    public Properties getProperties() {
        return readProperties();
    }


    public String getProperty(String propertyName) {
        return loadProperties().getProperty(propertyName);
    }

    public void fillAction(Consumer<String> action, String name) {
        String prop = null;
        try {
            prop = getProperty(name);
        } catch (Exception ignore) {}
        if (isBlank(prop)) {
            return;
        }
        if (isMvnProperty(prop)) {
            throw new RuntimeException(format("Can't read Maven property '%s'. Get value '%s'" + LINE_BREAK +
                "You need to add property in pom.xml and add <resources> block in <build>. " +
                "See example: https://github.com/jdi-templates/jdi-light-testng-template/blob/master/pom.xml",
            name, prop));
        }
        action.accept(prop);
    }

    private boolean isMvnProperty(String prop) {
        return prop.matches("^\\$\\{.+}");
    }

}