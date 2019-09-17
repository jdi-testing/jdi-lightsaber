package com.epam.jdi.tools;

import com.epam.jdi.tools.map.MapArray;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.jdi.tools.PathUtils.mergePath;
import static com.epam.jdi.tools.PropertyReader.getProperty;
import static java.lang.String.format;

public class JsonUtils {
    public static String readFileData(String filePath) {
        String data;
        try(InputStream inputStream = JsonUtils.class.getResourceAsStream(filePath.replaceAll("\\\\", "/"))) {
            data = readFromInputStream(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Can't read from stream!");
        } catch(NullPointerException npe) {
            throw new RuntimeException(format("Can't find file by path %s !", filePath));
        }
        return data;
    }

    private static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }
    public static Map<String, String> deserializeJsonToMap(String jsonName, String propertyName) {
        Gson gson = (new GsonBuilder()).create();
        Map<String, String> map = new HashMap<String, String>();
        String json = readFileData(mergePath(getProperty(propertyName), jsonName + ".json"));
        map = gson.fromJson(json, map.getClass());
        return map;
    }
    public static MapArray<String, String> getMapFromJson(String jsonName, String propertyName) {
        return MapArray.toMapArray(deserializeJsonToMap(jsonName, propertyName));
    }
    public static List<String> scanFolder(String folderName) {
        try {
            if (!folderName.contains(":"))
                folderName = mergePath(System.getProperty("user.dir"), folderName);
            return Files.walk(Paths.get(folderName))
                    .filter(Files::isRegularFile)
                    .map(f -> f.toAbsolutePath().toString())
                    .collect(Collectors.toList());
        } catch (Exception ex) { throw new RuntimeException("Can't get element: " + ex.getMessage()); }
    }
    public static MapArray<String, String> jsonToMap(List<String> filePaths) {
        MapArray<String, String> result = new MapArray<>();
        for (String filePath : filePaths)
            try {
                result.addAll(new ObjectMapper().readValue(new File(filePath), HashMap.class));
            } catch (IOException e) {
                throw new RuntimeException("Can't read elements from json");
            }
        return result;
    }
}
