package com.epam.jdi.tools;

import java.nio.file.Paths;

import static java.io.File.separator;

/**
 * Created by Roman_Iovlev on 4/17/2018.
 */
public class PathUtils {
    public static String path(String first, String... more) {
        return Paths.get(first, more).toAbsolutePath().toString();
    }
    public static String mergePath(String root, String... suffix) {
        if (suffix.length == 1 && suffix[0].contains("/")) {
            String path = suffix[0];
            if (path.charAt(0) == '/')
                path = path.substring(1);
            suffix = path.split("/");
        }
        if (root.charAt(root.length() - 1) == '/')
            root = root.substring(0, root.length()-1);
        return root + separator + String.join(separator, suffix);
    }
}
