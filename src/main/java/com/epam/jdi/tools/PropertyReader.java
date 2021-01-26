package com.epam.jdi.tools;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.func.JAction1;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Matcher.quoteReplacement;
import static org.apache.commons.lang3.StringUtils.isBlank;

public final class PropertyReader {
    private static String propertiesPath;
    private static volatile Properties properties;
    private static InputStream inputStream;

    private PropertyReader() {
    }
    public static String getPath() {
        if (isBlank(propertiesPath))
            return "";
        if (propertiesPath.charAt(0) != '/')
            propertiesPath = "/" + propertiesPath;
        return propertiesPath;
    }

    public static Properties readProperties() {
        properties = new Properties();
        try {
            inputStream = PropertyReader.class.getResourceAsStream(getPath());
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

    public static Properties loadProperties() {
        return properties != null ? properties : readProperties();
    }

    public static Properties getProperties(String path) {
        propertiesPath = path;
        return readProperties();
    }

    public static String getProperty(String propertyName) {
        String prop = null;
        try {
            prop = loadProperties().getProperty(propertyName);
        } catch (Throwable ignore) { }
        if (isBlank(prop)) return "";
        if (isMvnProperty(prop)) {
            prop = replaceProperty(prop);
        }
        return prop;
    }

    public static void fillAction(JAction1<String> action, String name) {
        String property = getProperty(name);
        if (isBlank(property)) return;
        action.execute(property);
    }
    private static boolean isMvnProperty(String prop) {
        return prop.matches("^\\$\\{.+}");
    }
    private static String replaceProperty(String property) {
        final Matcher matcher = Pattern.compile("\\$\\{([^}]*)}").matcher(property);
        final StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            final String pattern = matcher.group(1);
            String replacement = System.getProperty(pattern);
            if (replacement == null) {
                replacement = property;
            }
            matcher.appendReplacement(sb, quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}