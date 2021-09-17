package com.jdiai.tools;

import java.nio.file.Paths;

import static java.io.File.separator;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created by Roman_Iovlev on 4/17/2018.
 */
public class PathUtils {
    private PathUtils() { }
    public static String path(String first, String... more) {
        return Paths.get(first, more).toAbsolutePath().toString();
    }
    public static String mergePath(String root, String... suffix) {
        root = toStandardSlash(root);
        suffix = toStandardSlash(suffix);
        if (suffix.length == 1 && suffix[0].contains(separator)) {
            String path = suffix[0];
            if (path.charAt(0) == separator.charAt(0))
                path = path.substring(1);
            suffix = path.split(encodedSeparator());
        }
        if (root.charAt(root.length() - 1) == separator.charAt(0)) {
            root = root.substring(0, root.length() - 1);
        }
        return root + separator + String.join(separator, suffix);
    }
    public static String encodedSeparator() {
        return "\\"+separator;
    }
    private static String toStandardSlash(String s) {
        if (isBlank(s)) {
            return "";
        }
        return separator.equals("\\")
            ? s.replace("/", encodedSeparator())
            : s.replace("\\\\", separator);
    }
    private static String[] toStandardSlash(String... strings) {
        String[] result = new String[strings.length];
        for (int i=0; i<strings.length; i++)
            result[i] = toStandardSlash(strings[i]);
        return result;
    }
}
