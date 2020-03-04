package com.epam.jdi.tools;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.func.JFunc1;
import com.epam.jdi.tools.map.MapArray;
import com.epam.jdi.tools.pairs.Pair;

import java.text.MessageFormat;
import java.util.*;

import static com.epam.jdi.tools.LinqUtils.*;
import static com.epam.jdi.tools.ReflectionUtils.getFieldsDeep;
import static com.epam.jdi.tools.ReflectionUtils.getValueField;
import static java.lang.String.format;
import static java.lang.reflect.Array.get;
import static java.lang.reflect.Array.getLength;
import static java.util.Arrays.asList;

public final class PrintUtils {
    private PrintUtils() {
    }

    public static String print(Collection<String> list) {
        return print(list, ",", "%s");
    }

    public static String print(Collection<String> list, String separator) {
        return print(list, separator, "%s");
    }

    public static <T extends Enum> String printEnum(List<T> enums) {
        return enums != null ? String.join(",", select(enums, el -> format("%s", el))) : "";
    }

    public static String print(Collection<String> list, String separator, String format) {
        return list != null ? String.join(separator, select(list, el -> format(format, el))) : "";
    }

    public static String print(String[] list) {
        return print(list, ",", "%s");
    }

    public static String print(String[] list, String separator) {
        return print(list, separator, "%s");
    }

    public static String print(String[] list, String separator, String format) {
        return print(asList(list), separator, format);
    }
    public static <T> String print(Map<String, T> map, String separator, String format) {
        return print(toList(map, (k, v) -> MessageFormat.format(format, k, v)), separator, "%s");
    }
    public static <T> String print(Map<String, T> map, String separator) {
         return print(map, separator, "{0}:{1}");
    }
    public static <T> String print(Map<String, T> map) {
        return print(map, ";", "{0}:{1}");
    }

    public static String print(int[] list) {
        return print(list, ",", "%s");
    }

    public static String print(int[] list, String separator) {
        return print(list, separator, "%s");
    }

    public static String print(int[] list, String separator, String format) {
        List<String> result = new ArrayList<>();
        for (int i : list)
            result.add(Integer.toString(i));
        return print(result, separator, format);
    }

    public static String print(boolean[] list) {
        return print(list, ",", "%s");
    }

    public static String print(boolean[] list, String separator) {
        return print(list, separator, "%s");
    }

    public static String print(boolean[] list, String separator, String format) {
        List<String> result = new ArrayList<>();
        for (boolean i : list)
            result.add(Boolean.toString(i));
        return print(result, separator, format);
    }

    public static String printFields(Object obj) {
        return printFields(obj, "; ");
    }

    public static String printFields(Object obj, String separator) {
        String className = obj.getClass().getSimpleName();
        String params = print(select(where(getFieldsDeep(obj), field -> getValueField(field, obj) != null),
                field -> format("%s:%s", field.getName(), getValueField(field, obj))), separator, "%s");
        return format("%s(%s)", className, params);
    }
    public static <T> String print(Collection<T> list, JFunc1<T, String> func) {
        return print(map(list, func));
    }
    public static <T> String print(Collection<T> list, JFunc1<T, String> func, String separator) {
        return print(map(list, func), separator);
    }
    public static <T> String print(Collection<T> list,
           JFunc1<T, String> func, String separator, String format) {
        return print(map(list, func), separator, format);
    }
    public static String formatParams(String template, MapArray<String, String> params) {
        String result = template;
        for (Pair<String, String> param : params)
            result = result.replaceAll("\\{" + param.key + "}", param.value);
        return result;
    }

    public static String printList(Object obj) {
        List<?> list = (List<?>)obj;
        String result = "[";
        for (int i=0; i<list.size()-1;i++)
            result += list.get(i)+", ";
        return result + list.get(list.size()-1) + "]";
    }
    public static String printArray(Object array) {
        try {
            Object[] a = (Object[])array;
            if (a.length == 1)
                return a[0].toString();
            return Arrays.toString(a);
        } catch (Exception ex) {}
        try {
            int[] a = (int[])array;
            if (a.length == 1)
                return String.valueOf(a[0]);
            return Arrays.toString(a);
        } catch (Exception ex) {}
        try {
            boolean[] a = (boolean[])array;
            if (a.length == 1)
                return String.valueOf(a[0]);
            return Arrays.toString(a);
        } catch (Exception ex) {}
        try {
            float[] a = (float[])array;
            if (a.length == 1)
                return String.valueOf(a[0]);
            return Arrays.toString(a);
        } catch (Exception ex) {}
        try {
            double[] a = (double[])array;
            if (a.length == 1)
                return String.valueOf(a[0]);
            return Arrays.toString(a);
        } catch (Exception ex) {}
        try {
            char[] a = (char[])array;
            if (a.length == 1)
                return String.valueOf(a[0]);
            return Arrays.toString(a);
        } catch (Exception ex) {}
        try {
            byte[] a = (byte[])array;
            if (a.length == 1)
                return String.valueOf(a[0]);
            return Arrays.toString(a);
        } catch (Exception ex) {}
        return "Unsupported array";
    }

}