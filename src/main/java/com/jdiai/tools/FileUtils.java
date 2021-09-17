package com.jdiai.tools;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {
    private FileUtils() { }
    public static List<String> getFiles(String pathToDir) {
        try {
            return findFilesInFolder(pathToDir);
        }
        catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public static List<String> findFilesInFolder(String folderName) throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(folderName))) {
            return stream.filter(Files::isRegularFile)
                    .map(f -> f.toAbsolutePath().toString())
                    .collect(Collectors.toList());
        }
    }
}
