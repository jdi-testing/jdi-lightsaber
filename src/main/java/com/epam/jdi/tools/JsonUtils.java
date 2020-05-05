package com.epam.jdi.tools;

import com.epam.jdi.tools.map.MapArray;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.epam.jdi.tools.PathUtils.*;
import static com.epam.jdi.tools.PropertyReader.*;
import static com.epam.jdi.tools.map.MapArray.*;
import static java.lang.String.*;
import static org.apache.commons.lang3.StringUtils.*;

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
        Map<String, String> map = new HashMap<>();
        String property = getProperty(propertyName);
        if (isBlank(property))
            throw new RuntimeException(format("Can't get property: '%s'. Properties file path: '%s'", propertyName, getPath()));
        String json = readFileData(mergePath(property, jsonName + ".json"));
        map = gson.fromJson(json, map.getClass());
        return map;
    }
    public static MapArray<String, String> getMapFromJson(String jsonName, String propertyName) {
        return toMapArray(deserializeJsonToMap(jsonName, propertyName));
    }
    public static String beautifyJson(String json) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(json).getAsJsonObject());
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
