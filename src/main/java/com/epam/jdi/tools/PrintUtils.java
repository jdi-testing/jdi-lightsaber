package com.epam.jdi.tools;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.map.MapArray;
import com.epam.jdi.tools.pairs.Pair;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.epam.jdi.tools.LinqUtils.invokeBoolean;
import static com.epam.jdi.tools.LinqUtils.safeException;
import static com.epam.jdi.tools.ReflectionUtils.getFieldsDeep;
import static com.epam.jdi.tools.ReflectionUtils.getValueField;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public final class PrintUtils {
    private PrintUtils() { }

    public static String print(Collection<String> list) {
        return print(list, ",", "%s");
    }

    public static String print(Collection<String> list, String separator) {
        return print(list, separator, "%s");
    }

    public static <T extends Enum> String printEnum(List<T> enums) {
        return enums != null ? String.join(",", toList(enums, el -> format("%s", el))) : "";
    }

    public static String print(Collection<String> list, String separator, String format) {
        return list != null ? String.join(separator, toList(list, el -> format(format, el))) : "";
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
        for (int i : list) {
            result.add(Integer.toString(i));
        }
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
        for (boolean i : list) {
            result.add(Boolean.toString(i));
        }
        return print(result, separator, format);
    }

    public static String printFields(Object obj) {
        return printFields(obj, "; ");
    }

    public static String printFields(Object obj, String separator) {
        String className = obj.getClass().getSimpleName();
        String params = print(toList(getFieldsDeep(obj), field -> getValueField(field, obj) != null,
                field -> format("%s:%s", field.getName(), getValueField(field, obj))), separator, "%s");
        return format("%s(%s)", className, params);
    }
    public static <T> String print(Collection<T> list, Function<T, String> func) {
        return print(toList(list, func));
    }
    public static <T> String print(Collection<T> list, Function<T, String> func, String separator) {
        return print(toList(list, func), separator);
    }
    public static <T> String print(Collection<T> list,
           Function<T, String> func, String separator, String format) {
        return print(toList(list, func), separator, format);
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
        for (Object el : list) {
            result += el + ", ";
        }
        return result + list.get(list.size()-1) + "]";
    }
    public static String printArray(Object array) {
        try {
            Object[] a = (Object[])array;
            if (a.length == 1) {
                return a[0].toString();
            }
            return Arrays.toString(a);
        } catch (Exception ignored) { }
        try {
            int[] a = (int[])array;
            if (a.length == 1) {
                return String.valueOf(a[0]);
            }
            return Arrays.toString(a);
        } catch (Exception ignored) { }
        try {
            boolean[] a = (boolean[])array;
            if (a.length == 1) {
                return String.valueOf(a[0]);
            }
            return Arrays.toString(a);
        } catch (Exception ignored) { }
        try {
            float[] a = (float[])array;
            if (a.length == 1) {
                return String.valueOf(a[0]);
            }
            return Arrays.toString(a);
        } catch (Exception ignored) { }
        try {
            double[] a = (double[])array;
            if (a.length == 1) {
                return String.valueOf(a[0]);
            }
            return Arrays.toString(a);
        } catch (Exception ignored) { }
        try {
            char[] a = (char[])array;
            if (a.length == 1) {
                return String.valueOf(a[0]);
            }
            return Arrays.toString(a);
        } catch (Exception ignored) { }
        try {
            byte[] a = (byte[])array;
            if (a.length == 1) {
                return String.valueOf(a[0]);
            }
            return Arrays.toString(a);
        } catch (Exception ignored) { }
        return "Unsupported array";
    }

    private static RuntimeException getPrintException(Exception ex, int index) {
        return new RuntimeException(format("Can't print list: %s; Failed to print at index: %s", safeException(ex), index), ex);
    }
    private static <T, TR> List<TR> toList(Collection<T> list, Function<T, TR> func) {
        if (list == null) {
            throw new RuntimeException("Can't do select list. Collection is Null");
        }
        int index = 0;
        try {
            List<TR> result = new CopyOnWriteArrayList<>();
            for (T el : list) {
                result.add(func.apply(el));
                index ++;
            }
            return result;
        } catch (Exception ex) {
            throw getPrintException(ex, index);
        }
    }
    public static <K, V, TR> List<TR> toList(Map<K, V> map, BiFunction<K, V, TR> func) {
        if (map == null) {
            throw new RuntimeException("Can't do toList. Map is Null");
        }
        int index = 0;
        try {
            List<TR> result = new CopyOnWriteArrayList<>();
            for (Map.Entry<K,V> el : map.entrySet()) {
                result.add(func.apply(el.getKey(), el.getValue()));
                index ++;
            }
            return result;
        } catch (Exception ex) {
            throw getPrintException(ex, index);
        }
    }
    public static <T, R> List<R> toList(Collection<T> list, Function<T, Boolean> condition, Function<T, R> transform) {
        int index = 0;
        try {
            List<R> result = new ArrayList<>();
            for (T el : list) {
                if (invokeBoolean(condition, el)) {
                    result.add(transform.apply(el));
                }
                index++;
            }
            return result;
        } catch (Exception ex) {
            throw getPrintException(ex, index);
        }
    }
}
