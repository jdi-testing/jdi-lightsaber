package com.epam.jdi.tools;

import java.io.File;
import java.nio.file.Paths;

/**
 * Created by Roman_Iovlev on 4/17/2018.
 */
public class PathUtils {
    public static String path(String first, String... more) {
        return Paths.get(first, more).toAbsolutePath().toString();
    }
    public static String mergePath(String path, String... suffix) {
        return path + String.join(File.separator, suffix);
    }
}
