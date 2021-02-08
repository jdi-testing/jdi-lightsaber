package com.epam.jdi.tools;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.func.JAction1;
import com.epam.jdi.tools.func.JFunc2;
import com.epam.jdi.tools.map.MapArray;
import com.epam.jdi.tools.pairs.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.jdi.tools.ReflectionUtils.isClass;
import static com.epam.jdi.tools.ReflectionUtils.isInterface;
import static com.epam.jdi.tools.pairs.Pair.$;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Map.Entry;
import static java.util.stream.IntStream.rangeClosed;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class LinqUtils {
    private static final String NULL_COLLECTION = "Can't do where. Collection is Null";
    public static <T1, T2> boolean invokeBoolean(BiFunction<T1, T2, Boolean> func, T1 arg1, T2 arg2) {
        if (func == null) {
            return false;
        }
        Boolean result = func.apply(arg1, arg2);
        return result != null && result;
    }
    public static <T> boolean invokeBoolean(Function<T, Boolean> func, T arg) {
        if (func == null) {
            return false;
        }
        Boolean result = func.apply(arg);
        return result != null && result;
    }
    private LinqUtils() {
    }

    public static <T> List<T> copyList(Collection<T> list) {
        List<T> result = new ArrayList<>();
        result.addAll(list);
        return result;
    }
    public static <T> List<T> newList(T... array) {
        return stream(array).collect(Collectors.toList());
    }
    public static <K, V> Map<K, V> newMap(Pair<K, V>... pairs) {
        Map<K,V> map = new HashMap<>();
        for (Pair<K, V> pair : pairs) {
            map.put(pair.key, pair.value);
        }
        return map;
    }
    public static <K, V> Map<K, V> newMap(Object... keyValues) {
        if (keyValues.length % 10 != 0)
            throw exception("Failed to create newMap: amount of parameters should be even but " + keyValues.length, keyValues);
        Map<K,V> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i = i + 2) {
            K key = getTypeFromObject(keyValues[i]);
            V value = getTypeFromObject(keyValues[i + 1]);
            map.put(key, value);
        }
        return map;
    }
    private static <T> T getTypeFromObject(Object o) {
        try {
            return (T) o;
        }
         catch (Exception ex) {
            throw new RuntimeException("Failed to Cast object for newMap() method");
         }
    }
    public static <T, TR> List<TR> select(Collection<T> list, Function<T, TR> func) {
        if (list == null)
            throw new RuntimeException("Can't do select list. Collection is Null");
        try {
            List<TR> result = new CopyOnWriteArrayList<>();
            for (T el : list)
                result.add(func.apply(el));
            return result;
        } catch (Exception ex) {
            throw exception("Can't do select list: %s. %s", ex, list);
        }
    }
    public static <T, TR> List<TR> map(Collection<T> list, Function<T, TR> func) { return select(list, func); }
    public static <T, TR> List<TR> select(T[] array, Function<T, TR> func) {
        return select(asList(array), func);
    }
    public static <T, TR> List<TR> map(T[] array, Function<T, TR> func) { return select(array, func); }

    public static <K, V, R> List<R> selectMap(Map<K, V> map, Function<Map.Entry<K, V>, R> func) {
        if (map == null)
            throw new RuntimeException("Can't do selectMap. Collection is Null");
        try {
            List<R> result = new CopyOnWriteArrayList<>();
            for (Map.Entry<K,V> el : map.entrySet())
                result.add(func.apply(el));
            return result;
        } catch (Exception ex) {
            throw exception("Can't do selectMap: %s. %s", ex, map);
        }
    }

    public static <K, V, TR> Map<K, TR> select(Map<K, V> map, Function<V, TR> func) {
        if (map == null)
            throw new RuntimeException("Can't do select map. Collection is Null");
        try {
            Map<K, TR> result = new HashMap<>();
            for (Map.Entry<K, V> el : map.entrySet())
                result.put(el.getKey(), func.apply(el.getValue()));
            return result;
        } catch (Exception ex) {
            throw exception("Can't do select map: %s. %s", ex, map);
        }
    }
    public static <K, V, TR> Map<K, TR> map(Map<K, V> map, Function<V, TR> func) {
        return select(map, func);
    }
    public static <N, T> Map<N, T> toMap(List<T> list, Function<T, N> nameFunc) {
        Map<N, T> map = new HashMap<>();
        for(T el : list)
            map.put(nameFunc.apply(el), el);
        return map;
    }
    public static <K, V, T> Map<K, V> toMap(List<T> list, Function<T, K> key, Function<T, V> value) {
        Map<K, V> map = new HashMap<>();
        for(T el : list)
            map.put(key.apply(el), value.apply(el));
        return map;
    }
    public static <N, T> Map<N, T> toMap(T[] list, Function<T, N> nameFunc) {
        return toMap(asList(list), nameFunc);
    }
    public static <K, V, T> Map<K, V> toMap(T[] list, Function<T, K> key, Function<T, V> value) {
        return toMap(asList(list), key, value);
    }
    public static <K, V, TR> List<TR> toList(Map<K, V> map, JFunc2<K, V, TR> func) {
        if (map == null)
            throw new RuntimeException("Can't do toList. Map is Null");
        try {
            List<TR> result = new CopyOnWriteArrayList<>();
            for (Map.Entry<K,V> el : map.entrySet())
                result.add(func.apply(el.getKey(), el.getValue()));
            return result;
        } catch (Exception ex) {
            throw exception("Can't do toList: %s. %s", ex, map);
        }
    }

    public static <T> List<T> where(Collection<T> list, Function<T, Boolean> func) {
        if (list == null)
            throw new RuntimeException(NULL_COLLECTION);
        try {
            List<T> result = new ArrayList<>();
            for (T el : list)
                if (invokeBoolean(func, el))
                    result.add(el);
            return result;
        } catch (Exception ex) {
            throw exception("Can't do where: %s. %s", ex, list);
        }
    }
    public static <T> List<T> filter(Collection<T> list, Function<T, Boolean> func) {
        return where(list, func);
    }
    public static <T> List<T> where(T[] list, Function<T, Boolean> func) {
        return where(asList(list), func);
    }
    public static <T> List<T> filter(T[] list, Function<T, Boolean> func) {
        return where(list, func);
    }
    public static <T> MapArray<String, List<T>> partition(List<T> list, Function<T, Boolean> split) {
        return partition(list, $("match", split));
    }
    public static <T> MapArray<String, List<T>> partition(List<T> list, Pair<String, Function<T, Boolean>>... matchers) {
        if (list == null)
            throw new RuntimeException(NULL_COLLECTION);
        try {
            MapArray<String, List<T>> result = new MapArray<>();
            for (Pair<String, Function<T, Boolean>> matcher : matchers) {
                result.add(matcher.key, new ArrayList<>());
            }
            result.add("other", new ArrayList<>());
            for (T el : list) {
                Pair<String, Function<T, Boolean>> matched = first(matchers, m -> m.value.apply(el));
                String key = matched != null ? matched.key : "other";
                result.get(key).add(el);
            }
            return result;
        } catch (Exception ex) {
            throw exception("Can't do partition: %s. %s", ex, list);
        }
    }
    public static <T> MapArray<String, List<T>> partition(T[] array, Function<T, Boolean> split) { return partition(array, split); }

    public static <K, V> Map<K, V> where(Map<K, V> map, Function<Map.Entry<K, V>, Boolean> func) {
        if (map == null)
            throw new RuntimeException(NULL_COLLECTION);
        try {
            Map<K, V> result = new HashMap<>();
            for (Map.Entry<K,V> el : map.entrySet())
                if (invokeBoolean(func, el))
                    result.put(el.getKey(), el.getValue());
            return result;
        } catch (Exception ex) {
            throw exception("Can't do where: %s. %s", ex, map);
        }
    }
    public static <K, V> Map<K, V> filter(Map<K, V> map, Function<Map.Entry<K, V>, Boolean> func) {
        return where(map, func);
    }
    public static <T> void ifDo(Collection<T> list, Function<T, Boolean> condition, JAction1<T> action) {
        try {
            for (T el : list)
                if (invokeBoolean(condition, el))
                    action.invoke(el);
        } catch (Exception ex) {
            throw exception("Can't perform ifDo: %s. %s", ex, list);
        }
    }
    public static <T> void ifDo(T[] array, Function<T, Boolean> condition, JAction1<T> action) {
        ifDo(asList(array), condition, action);
    }
    public static <K, V> void ifDo(Map<K, V> map, Function<Map.Entry<K, V>, Boolean> condition, JAction1<V> action) {
        try {
        for (Map.Entry<K,V> el : map.entrySet())
            if (invokeBoolean(condition, el))
                action.invoke(el.getValue());
        } catch (Exception ex) {
            throw exception("Can't perform ifDo: %s. %s", ex, map);
        }
    }

    public static <T, R> List<R> ifSelect(Collection<T> list, Function<T, Boolean> condition, Function<T, R> transform) {
        try {
            List<R> result = new ArrayList<>();
            for (T el : list)
                if (invokeBoolean(condition, el))
                    result.add(transform.apply(el));
            return result;
        } catch (Exception ex) {
            throw exception("Can't perform ifSelect: %s. %s", ex, list);
        }
    }
    public static <T, R> List<R> ifSelect(T[] array,
            Function<T, Boolean> condition, Function<T, R> transform) {
        return ifSelect(asList(array), condition, transform);
    }
    public static <K, V, T> List<T> ifSelect(Map<K, V> map, JFunc2<K, V, Boolean> condition, Function<V, T> transform) {
        try {
            List<T> result = new ArrayList<>();
            for (Map.Entry<K,V> el : map.entrySet())
                if (invokeBoolean(condition, el.getKey(), el.getValue()))
                    result.add(transform.apply(el.getValue()));
            return result;
        } catch (Exception ex) {
            throw exception("Can't perform ifSelect: %s. %s", ex, map);
        }
    }

    public static <T> void foreach(Collection<T> list, JAction1<T> action) {
        if (list == null)
            throw new RuntimeException("Can't do foreach. Collection is Null");
        try {
            for (T el : list)
                action.invoke(el);
        } catch (Exception ex) {
            throw exception("Can't do foreach: %s. %s", ex, list);
        }
    }

    public static <T> void foreach(T[] list, JAction1<T> action) {
        foreach(asList(list), action);
    }

    public static <K, V> void foreach(Map<K, V> map, JAction1<Map.Entry<K, V>> action) {
        if (map == null)
            throw new RuntimeException("Can't do foreach. Collection is Null");
        try {
            for (Entry<K,V> e : map.entrySet())
                action.invoke(e);
        } catch (Exception ex) {
            throw exception("Can't do foreach: %s. %s", ex, map);
        }
    }

    public static <T> boolean any(Collection<T> list, Function<T, Boolean> func) {
        return first(list, func) != null;
    }
    public static <T> boolean any(T[] list, Function<T, Boolean> func) {
        return first(list, func) != null;
    }
    public static <T> T single(Collection<T> list, Function<T, Boolean> func) {
        if (isEmpty(list))
            return null;
        T found = null;
        try {
            for (T el : list)
                if (invokeBoolean(func, el)) {
                    if (found != null) return null;
                    else found = el;
                }
        } catch (Exception ex) {
            throw exception("Can't do single: %s. %s", ex, list);
        }
        return found;
    }
    public static <T> T single(T[] array, Function<T, Boolean> func) {
        return single(asList(array), func);
    }
    public static <T> boolean all(Collection<T> list, Function<T, Boolean> func) {
        if (list == null)
            return true;
        try {
            for (T el : list)
                if (!invokeBoolean(func, el))
                    return false;
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
    public static <T> boolean all(T[] array, Function<T, Boolean> func) {
        return all(asList(array), func);
    }
    private static <T> int getStartIndex(List<T> list) {
        try {
            return isInterface(list.getClass(), HasStartIndex.class)
                ? ((HasStartIndex) list).getStartIndex()
                : 0;
        } catch (Exception ex) { return 1; }
    }
    public static <T> int firstIndex(List<T> list, Function<T, Boolean> func) {
        if (isEmpty(list))
            return -1;
        try {
            int i = getStartIndex(list);
            for (T element : list) {
                if (invokeBoolean(func, element))
                    return i;
                i++;
            }
        } catch (Exception ignore) { }
        return -1;
    }

    public static <T> int firstIndex(T[] array, Function<T, Boolean> func) {
        try {
            if (array == null || array.length == 0)
                return -1;
            for (int i = 0; i < array.length; i++)
                if (invokeBoolean(func, array[i]))
                    return i;
        } catch (Exception ignore) { }
        return -1;
    }

    public static <T> T first(Collection<T> list) {
        if (isEmpty(list))
            throw new RuntimeException("Can't do first. Collection is Null or empty");
        return list.iterator().next();
    }

    public static <T> T first(T[] list) {
        return first(asList(list));
    }

    public static <K, V> V first(Map<K, V> map) {
        if (map == null || map.size() == 0)
            throw new RuntimeException("Can't do first map. Collection is Null");
        return map.entrySet().iterator().next().getValue();
    }

    public static <T> T first(Collection<T> list, Function<T, Boolean> func) {
        if (isEmpty(list))
            return null;
        try {
            for (T el : list)
                if (invokeBoolean(func, el))
                    return el;
        } catch (Exception ex) {
            throw exception("Can't do first list: %s. %s", ex, list);
        }
        return null;
    }

    public static <T> T first(T[] list, Function<T, Boolean> func) {
        return first(asList(list), func);
    }

    public static <K, V> V first(Map<K, V> map, Function<K, Boolean> func) {
        if (map == null || map.size() == 0)
            throw new RuntimeException("Can't do first map. Collection is Null or empty");
        try {
            for (Map.Entry<K, V> el : map.entrySet())
                if (invokeBoolean(func, el.getKey()))
                    return el.getValue();
        } catch (Exception ex) {
            throw exception("Can't do first map: %s. %s", ex, map);
        }
        return null;
    }

    public static <K, V> V first(MapArray<K, V> map, Function<K, Boolean> func) {
        if (isEmpty(map)) {
            throw new RuntimeException("Can't do first map. Collection is Null or empty");
        }
        try {
            for (Pair<K, V> pair : map.pairs)
                if (invokeBoolean(func, pair.key))
                    return pair.value;
        } catch (Exception ex) {
            throw exception("Can't do first map: %s. %s", ex, map);
        }
        return null;
    }

    public static <T> T last(Collection<T> list) {
        if (isEmpty(list)) {
            throw new RuntimeException("Can't do last list. Collection is Null or empty");
        }
        T result = null;
        for (T el : list)
            result = el;
        return result;
    }

    public static <T> T last(T[] list) {
        return last(asList(list));
    }

    public static <T> T last(Collection<T> list, Function<T, Boolean> func) {
        if (isEmpty(list))
            throw new RuntimeException("Can't do last list. Collection is Null");
        T result = null;
        try {
            for (T el : list)
                if (invokeBoolean(func, el))
                    result = el;
        } catch (Exception ex) {
            throw exception("Can't do last list: %s. %s", ex, list);
        }
        return result;
    }

    public static <T> T last(T[] list, Function<T, Boolean> func) {
        return last(asList(list), func);
    }

    public static <T> T[] toArray(Collection<T> collection, Function<Integer, T[]> constructor) {
        if (collection == null)
            throw new RuntimeException("Can't do toStringArray. Collection is Null");
        return collection.toArray(constructor.apply(collection.size()));
    }
    public static String[] toStringArray(Collection<String> collection) {
        if (collection == null)
            throw new RuntimeException("Can't do toStringArray. Collection is Null");
        return collection.toArray(new String[0]);
    }

    public static Integer[] toIntegerArray(Collection<Integer> collection) {
        if (collection == null)
            throw new RuntimeException("Can't do toIntegerArray. Collection is Null");
        Integer[] result = new Integer[collection.size()];
        int i = 0;
        for (Integer el : collection)
            result[i++] = el;
        return result;
    }
    public static int[] toIntArray(Collection<Integer> collection) {
        if (collection == null)
            throw new RuntimeException("Can't do toIntArray. Collection is Null");
        int[] result = new int[collection.size()];
        Integer i = 0;
        for (int el : collection)
            result[i++] = el;
        return result;
    }

    public static int getIndex(String[] array, String value) {
        if (array == null || array.length == 0)
            return -1;
        for (int i = 0; i < array.length; i++)
            if (array[i].equals(value))
                return i;
        return -1;
    }

    public static int getIndex(List<String> list, String value) {
        if (list == null)
            return -1;
        int i = getStartIndex(list);
        for (String element : list) {
            if (element.equals(value))
                return i;
            i++;
        }
        return -1;
    }

    public static List<Integer> toList(int... nums) {
        List<Integer> result = new ArrayList<>();
        for (int num : nums) result.add(num);
        return result;
    }
    public static List<Boolean> toList(boolean... nums) {
        List<Boolean> result = new ArrayList<>();
        for (boolean num : nums) result.add(num);
        return result;
    }
    public static List<Long> toList(long... nums) {
        List<Long> result = new ArrayList<>();
        for (long num : nums) result.add(num);
        return result;
    }
    public static List<Double> toList(double... nums) {
        List<Double> result = new ArrayList<>();
        for (double num : nums) result.add(num);
        return result;
    }
    public static List<Float> toList(float... nums) {
        List<Float> result = new ArrayList<>();
        for (float num : nums) result.add(num);
        return result;
    }
    public static List<Byte> toList(byte... nums) {
        List<Byte> result = new ArrayList<>();
        for (byte num : nums) result.add(num);
        return result;
    }
    public static <T> List<T> listCopy(List<T> list, int from) {
        return listCopy(list, from, list.size() - 1);
    }

    public static <T> List<T> listCopy(List<T> list, int from, int to) {
        if (from < 0)
            from = list.size() + from - 1;
        if (to < 0)
            to = list.size() + to - 1;
        if (from > to)
            throw new RuntimeException(format("'from' should be more than 'to' %s>%s", from, to));
        List<T> result = new ArrayList<>();
        for (int i = from; i <= to; i++)
            result.add(list.get(i));
        return result;
    }
    public static List<Integer> listOfRange(int start, int end) {
        return rangeClosed(start, end).boxed().collect(Collectors.toList());
    }

    public static <T> List<T> listCopyUntil(List<T> list, int to) {
        return listCopy(list, 0, to);
    }

    public static <T, R> List<R> selectMany(List<T> list, Function<T, List<R>> func) {
        try {
            List<R> result = new ArrayList<>();
            for (T el : list)
                result.addAll(func.apply(el));
            return result;
        } catch (Exception ex) {
            throw exception("Can't do selectMany list: %s. %s", ex, list);
        }
    }

    public static <T> List<T> selectManyArray(List<T> list, Function<T, T[]> func) {
        try {
                List<T> result = new ArrayList<>();
            for (T el : list)
                result.addAll(Arrays.asList(func.apply(el)));
            return result;
        } catch (Exception ex) {
            throw exception("Can't do selectManyArray: %s. %s", ex, list);
        }
    }

    public static <T> boolean listEquals(List<T> list1, List<T> list2) {
        if (list1 == null && list2 == null)
            return true;
        if (list1 == null || list2 == null
            || list1.size() != list2.size())
            return false;
        List<T> expectedList = new ArrayList<>(list2);
        for (T el1 : list1) {
            boolean removed = false;
            for (T el2 : expectedList) {
                if (el1.equals(el2)) {
                    removed = expectedList.remove(el2);
                    break;
                }
            }
            if (!removed)
                return false;
        }
        return true;
    }
    public static <T> boolean listEquals(List<T> list, T[] array) {
        return listEquals(list, asList(array));
    }
    public static <T> T get(List<T> list, int i) {
        int index = i >= 0 ? i : list.size() + i;
        return index >= 0 && index < list.size()
                ? list.get(index)
                : null;
    }
    public static <T> T get(T[] array, int i) {
        return asList(array).get(i);
    }

    public static <T> boolean contains(List<T> list, T t) {
        return first(list, el -> el.equals(t)) != null;
    }
    public static <T> boolean contains(T[] list, T t) {
        return first(list, el -> el.equals(t)) != null;
    }

    public static <T> T valueOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    public static <T> List<T> list(T... elements) {
        return select(elements, e -> e);
    }

    public static boolean isSorted(Object first, Object second, boolean asc, boolean strict) {
        return asc ? isAscending(first, second, strict) : isAscending(second, first, strict);
    }

    public static boolean isAscending(Object first, Object second, boolean strict) {
        try {
            byte a = (byte) first;
            byte b = (byte) second;
            return strict ? a < b : a <= b;
        } catch (Exception ignored) { }
        try {
            int a = (int) first;
            int b = (int) second;
            return strict ? a < b : a <= b;
        } catch (Exception ignored) { }
        try {
            long a = (long) first;
            long b = (long) second;
            return strict ? a < b : a <= b;
        } catch (Exception ignored) { }
        try {
            float a = (float) first;
            float b = (float) second;
            return strict ? a < b : a <= b;
        } catch (Exception ignored) { }
        try {
            double a = (double) first;
            double b = (double) second;
            return strict ? a < b : a <= b;
        } catch (Exception ignored) { }
        try {
            String a = first.toString();
            String b = second.toString();
            int compare = a.compareTo(b);
            return strict ? compare < 0 : compare <= 0;
        } catch (Exception ignored) { }
        throw new RuntimeException(format("isAscending failed because values first='%s' second = '%s' are not comparable", first, second));
    }
    private static RuntimeException exception(String tmpl, Exception ex, Map<?, ?> map) {
        try {
            List<String> m = selectMap(map, e -> e.getKey().toString() + ":" + e.getValue());
            return new RuntimeException(format(tmpl, safeException(ex), safePrintCollection(m)));
        } catch (Exception exception) {
            return new RuntimeException(tmpl, ex);
        }
    }

    public static String safePrintCollection(Collection<?> list) {
        try {
            String result = "[";
            for (Object el : list) {
                result += tryToString(el) + ";";
            }
            return result.substring(0, result.length() - 1) + "]";
        } catch (Exception ex) {
            return "Failed to print list";
        }
    }
    private static String tryToString(Object obj) {
        try {
            return obj.toString();
        } catch (Exception ex) {
            return "Failed to Print";
        }
    }
    private static RuntimeException exception(String tmpl, Exception ex, Collection<?> list) {
        try {
            return new RuntimeException(format(tmpl, safeException(ex), safePrintCollection(list)));
        } catch (Exception exception) {
            return new RuntimeException(tmpl);
        }
    }
    private static RuntimeException exception(String msg, Collection<?> list) {
        try {
            return new RuntimeException(msg + "| " + safePrintCollection(list));
        } catch (Exception exception) {
            return new RuntimeException(msg);
        }
    }
    private static RuntimeException exception(String msg, Object[] list) {
        return exception(msg, asList(list));
    }

    public static String safeException(Throwable ex) {
        String msg = ex.getMessage();
        try {
            if (isBlank(msg) && isClass(ex.getClass(), InvocationTargetException.class))
                msg = ((InvocationTargetException) ex).getTargetException().getMessage();
        } catch (Throwable ignore) { }
        return isNotBlank(msg) ? msg : ex.toString();
    }
}