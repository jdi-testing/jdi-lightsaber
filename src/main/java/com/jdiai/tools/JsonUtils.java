package com.jdiai.tools;

import com.jdiai.tools.map.MapArray;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jdiai.tools.FileUtils.findFilesInFolder;
import static com.jdiai.tools.PathUtils.mergePath;
import static com.jdiai.tools.map.MapArray.toMapArray;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class JsonUtils {
    private JsonUtils() { }
    public static String readFileData(String filePath) {
        String data;
        try(InputStream inputStream = JsonUtils.class.getResourceAsStream(filePath.replace("\\\\", "/"))) {
            data = readFromInputStream(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Can't read from stream!");
        } catch (NullPointerException npe) {
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
        Map<String, String> map = new HashMap<>();
        String property = PropertyReader.getProperty(propertyName);
        if (isBlank(property))
            throw new RuntimeException(String.format("Can't get property: '%s'. Properties file path: '%s'", propertyName, PropertyReader.getPath()));
        String json = readFileData(mergePath(property, jsonName + ".json"));
        map = gson.fromJson(json, map.getClass());
        return map;
    }
    public static MapArray<String, String> getMapFromJson(String jsonName, String propertyName) {
        return toMapArray(deserializeJsonToMap(jsonName, propertyName));
    }
    public static String beautifyJson(String json) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(json).getAsJsonObject());
    }
    public static List<String> scanFolder(String folderName) {
        try {
            if (!folderName.contains(":"))
                folderName = mergePath(System.getProperty("user.dir"), folderName);
            return findFilesInFolder(folderName);
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

    public static int getInt(Object value) {
        try {
            return (int)value;
        } catch (Exception ignore) { }
        try {
            return ((Double)value).intValue();
        } catch (Exception ignore) { }
        try {
            return ((Long)value).intValue();
        } catch (Exception ignore) { }
        try {
            return ((Float)value).intValue();
        } catch (Exception ignore) { }
        return -1;
    }
    public static double getDouble(Object obj) {
        if (obj == null) return 0.0;
        try {
            return (double) obj;
        } catch (Exception ex) {
            return ((Long) obj).doubleValue();
        }
    }
}
