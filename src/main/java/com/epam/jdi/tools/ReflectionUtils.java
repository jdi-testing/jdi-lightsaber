package com.epam.jdi.tools;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.func.JFunc1;
import com.epam.jdi.tools.map.MapArray;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.epam.jdi.tools.LinqUtils.*;
import static com.epam.jdi.tools.map.MapArray.IGNORE_NOT_UNIQUE;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.asList;

public final class ReflectionUtils {
    private ReflectionUtils() { }
    public static boolean isClass(Field field, Class<?> expected) {
        return isClass(field.getType(), expected);
    }

    public static boolean isClass(Class<?> t, Class<?> expected) {
        if (expected == Object.class)
            return true;
        Class<?> type = t;
        while (type != null && type != Object.class)
            if (type == expected) return true;
            else type = type.getSuperclass();
        return false;
    }

    public static boolean isClass(Class<?> type, Class<?>... expected) {
        for (Class<?> expectedType : expected) {
            Class<?> actualType = type;
            if (expectedType == Object.class) return true;
            while (actualType != null && actualType != Object.class)
                if (actualType == expectedType) return true;
                else actualType = actualType.getSuperclass();
        }
        return false;
    }

    public static boolean isInterface(Field field, Class<?> expected) {
        return isInterface(field.getType(), expected);
    }

    public static boolean isInterface(Class<?> type, Class<?> expected) {
        if (type == null || expected == null || type == Object.class)
            return false;
        if (type == expected)
            return true;
        List<Class> interfaces = asList(type.getInterfaces());
        return any(interfaces, i -> isInterface(i, expected)) || isInterface(type.getSuperclass(), expected);
    }

    public static MapArray<String, Object> getAllFields(Object obj) {
        IGNORE_NOT_UNIQUE = true;
        MapArray<String, Object> map = new MapArray<>(getFields(obj, Object.class),
            Field::getName, f -> getValueField(f, obj));
        IGNORE_NOT_UNIQUE = false;
        return map;
    }
    public static List<Field> getFields(Object obj) {
        return getFields(obj, new Class<?>[] {}, null);
    }
    public static List<Field> getFieldsDeep(Object obj) {
        return getFields(obj, new Class<?>[] { }, Object.class);
    }
    public static List<Field> getFields(Object obj, Class<?>... stopTypes) {
        return getFields(obj, stopTypes, Object.class);
    }
    public static List<Field> getFields(Object obj, Class<?>[] filterTypes, Class<?>... stopTypes) {
        return getFields(obj, getFieldsDeep(obj.getClass(), stopTypes), filterTypes, f -> !isStatic(f.getModifiers()));
    }

    public static List<Field> getFieldsExact(Class cl) {
        return asList(cl.getDeclaredFields());
    }
    public static List<Field> getFieldsExact(Class cl, JFunc1<Field, Boolean> filter) {
        return filter(getFieldsExact(cl), filter);
    }
    public static List<Field> getFieldsExact(Class cl, Class<?> stopType) {
        return getFieldsExact(cl, f -> f.getType() == stopType);
    }
    public static List<Field> getFieldsExact(Object obj, Class<?> stopType) {
        return getFieldsExact(obj.getClass(), stopType);
    }
    public static List<Field> getFields(List<Field> fields, Class<?>[] filterTypes, Function<Field, Boolean> filter) {
        return getFields(null, fields, filterTypes, filter);
    }
    public static List<Field> getFieldsInterfaceOf(Object obj, Class<?>... filterTypes) {
        return getFields(obj, asList(obj.getClass().getDeclaredFields()), filterTypes, f -> true);
    }
    public static List<Field> getFields(Object obj, List<Field> fields, Class<?>[] filterTypes, Function<Field, Boolean> filter) {
        List<Field> result = new ArrayList<>();
        for (Field field : fields) {
            if (filter.apply(field)) {
                Object value = null;
                if (obj != null)
                     value = getValueField(field, obj);
                if (value != null) {
                    if (isExpectedClass(value, filterTypes))
                        result.add(field);
                } else if (isExpectedClass(field, filterTypes))
                    result.add(field);
            }
        }
        return result;
    }

    public static List<Field> getFieldsDeep(Class<?> type, Class<?>... stopTypes) {
        if (stopTypes == null || stopTypes.length == 0)
            return asList(type.getDeclaredFields());
        return stopTypes.length == 1 && stopTypes[0] == Object.class
            ? getFieldsDeep3(type)
            : getFieldsDeep2(type, stopTypes);
    }
    private static List<Field> getFieldsDeep3(Class<?> type) {
        if (type == Object.class)
            return new ArrayList<>();
        List<Field> result = new ArrayList<>(asList(type.getDeclaredFields()));
        result.addAll(getFieldsDeep3(type.getSuperclass()));
        return result;
    }
    private static List<Field> getFieldsDeep2(Class<?> type, Class<?>[] stopTypes) {
        if (asList(stopTypes).contains(type) || type == Object.class)
            return new ArrayList<>();
        List<Field> result = new ArrayList<>(asList(type.getDeclaredFields()));
        result.addAll(getFieldsDeep2(type.getSuperclass(), stopTypes));
        return result;
    }

    public static <T> T getFirstField(Object obj, Class<?>... types) {
        return (T) getValueField(first(obj.getClass().getDeclaredFields(), field -> isExpectedClass(field, types)), obj);
    }
    private static boolean isExpectedClass(Field field, Class<?>... types) {
        if (types == null || types.length == 0)
            return true;
        for (Class<?> type : types)
            if (isClass(field, type) || isInterface(field, type))
                return true;
        return false;
    }
    private static boolean isExpectedClass(Object obj, Class<?>... types) {
        if (obj == null)
            return false;
        if (types == null || types.length == 0)
            return true;
        for (Class<?> type : types)
            if (isClass(obj.getClass(), type) || isInterface(obj.getClass(), type))
                return true;
        return false;
    }

    public static Object getValueField(Field field, Object obj) {
        field.setAccessible(true);
        try {
            return field.get(obj);
        } catch (Exception ex) {
            throw new RuntimeException(format("Can't get field '%s' value", field.getName()));
        }
    }

    public static Object convertStringToType(String value, Field field)
    {
        Class<?> clazz = field.getType();
        if (clazz.isAssignableFrom(String.class)|| value == null)
            return value;
        if (clazz.isAssignableFrom(Byte.class))
            return Byte.parseByte(value);
        if (clazz.isAssignableFrom(Short.class))
            return Short.parseShort(value);
        if (clazz.isAssignableFrom(Integer.class))
            return Integer.parseInt(value);
        if (clazz.isAssignableFrom(Long.class))
            return Long.parseLong(value);
        if (clazz.isAssignableFrom(Float.class))
            return Float.parseFloat(value);
        if (clazz.isAssignableFrom(Double.class))
            return Float.parseFloat(value);
        if (clazz.isAssignableFrom(Boolean.class))
            return Boolean.parseBoolean(value);

        throw new IllegalArgumentException("Can't parse field " + field.getName() + ". Type [" + clazz + "] is unsupported");
    }

    public static <T> Class<T> checkEntityIsNotNull(Class<T> entityClass) {
        if (entityClass == null)
            throw new IllegalArgumentException("Entity type was not specified");
        return entityClass;
    }

    public static <T> T newEntity(Class<T> entityClass) {
        try {
            return entityClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Can't instantiate " + entityClass.getSimpleName() +
                    ". You must have empty constructor to do this");
        }
    }
}