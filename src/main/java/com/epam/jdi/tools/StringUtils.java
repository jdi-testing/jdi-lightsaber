package com.epam.jdi.tools;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.map.MapArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.epam.jdi.tools.ReflectionUtils.getAllFields;
import static java.lang.Character.*;
import static java.util.Arrays.asList;
import static java.util.regex.Matcher.quoteReplacement;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public final class StringUtils {
    public static final String LINE_BREAK = System.getProperty("line.separator");

    public static boolean namesEqual(String name1, String name2) {
        return name1.toLowerCase().replace(" ", "").equals(name2.toLowerCase().replace(" ", ""));
    }

    public static String msgFormat(String template, List<Object> args) {
        String result = template;
        for (int i=0;i<args.size();i++)
            if (template.contains("{"+i+"}"))
                result = result.replaceAll("\\{"+i+"}", args.get(i).toString());
        return result;
    }
    public static String msgFormat(String template, Object... args) {
        String result = template;
        for (int i=0;i<args.length;i++)
            if (template.contains("{"+i+"}"))
                result = result.replaceAll("\\{"+i+"}", args[i].toString());
        return result;
    }
    public static String charSequenceToString(CharSequence... value) {
        String result = "";
        for (CharSequence c : value)
            result += c;
        return result;

    }
    public static String msgFormat(String template, Object obj) {
        return msgFormat(template, getAllFields(obj));
    }
    public static String msgFormat(String template, MapArray<String, Object> args) {
        final Matcher matcher = Pattern.compile("\\{([^}]*)}").matcher(template);
        final StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            final String pattern = matcher.group(1);
            Object replacement = args.get(pattern);
            if (replacement == null) replacement = matcher.group();
            matcher.appendReplacement(sb, quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    public static boolean contains(String string, String[] strings) {
        return contains(string, asList(strings));
    }
    public static boolean contains(String string, List<String> strings) {
        for (String s : strings)
            if (!string.contains(s)) return false;
        return true;
    }
    //someVariable Name-Field -> some variable name field
    public static String splitLowerCase(String value) {
        String result = "";
        for (int i = 0; i < value.length(); i++) {
            if (isUpperCase(value.charAt(i)))
                result += " ";
            result += toLowerCase(value.charAt(i));
        }
        return result;
    }

    //someVariable IDName-Field -> Some Variable ID Name Field
    public static String splitCamelCase(String value) {
        String result = (value.charAt(0) + "").toUpperCase();
        for (int i = 1; i < value.length() - 1; i++)
            result += (isUpperCase(value.charAt(i)) && (
                    isLowerCase(value.charAt(i+1)) || isLowerCase(value.charAt(i-1)))
                    ? " " : "") + value.charAt(i);
        return result + value.charAt(value.length() - 1);
    }
    public static String splitHyphen(String value) {
        if (isEmpty(value)) return "";
        String result = Character.toString(toLowerCase(value.charAt(0)));
        for (int i = 1; i < value.length(); i++) {
            char symbol = value.charAt(i);
            if (isUpperCase(symbol))
                result += "-";
            if (Character.toString(symbol).matches("[a-zA-Z0-9]"))
                result += toLowerCase(symbol);
        }
        return result;
    }

    public static String correctPath(String path) {
        return path.replace("\\", File.separator);
    }
    public static String format(String s, Object... args) {
        return args.length > 0 ? String.format(s, args) : s;
    }

    public static List<String> inputStreamToList(InputStream stream) {
        List<String> list = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        } catch (Exception ex) { throw new RuntimeException("Can't read Input Stream: " + ex.getMessage()); }
        return list;
    }

    private StringUtils() {}
}