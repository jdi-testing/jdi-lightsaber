package com.epam.jdi.tools;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.func.JFunc1;
import com.epam.jdi.tools.map.MapArray;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.epam.jdi.tools.LinqUtils.*;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

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
        return isInterface(t, expected);
    }

    public static boolean isClassOr(Class<?> type, Class<?>... expected) {
        for (Class<?> expectedType : expected) {
            if (isClass(type, expectedType))
                return true;
        }
        return false;
    }

    public static boolean isClassAnd(Class<?> type, Class<?>... expected) {
        for (Class<?> expectedType : expected) {
            if (!isClass(type, expectedType))
                return false;
        }
        return true;
    }
    public static boolean isInterface(Field field, Class<?> expected) {
        return isInterface(field.getType(), expected);
    }

    public static boolean isInterfaceAnd(Class<?> type, Class<?>... interfaces) {
        for (Class<?> i : interfaces) {
            if (!isInterface(type, i))
                return false;
        }
        return true;
    }
    public static boolean isInterfaceAOr(Class<?> type, Class<?>... interfaces) {
        for (Class<?> i : interfaces) {
            if (isInterface(type, i))
                return true;
        }
        return false;
    }
    public static boolean isInterface(Class<?> type, Class<?> expected) {
        if (type == null || expected == null || type == Object.class)
            return false;
        if (type == expected)
            return true;
        List<Class<?>> interfaces = asList(type.getInterfaces());
        return any(interfaces, i -> isInterface(i, expected)) || isInterface(type.getSuperclass(), expected);
    }

    public static MapArray<String, Object> getAllFields(Object obj) {
        return new MapArray<>(getFields(obj, Object.class),
            Field::getName, f -> getValueField(f, obj), true);
    }
    public static List<Field> getFields(Object obj) {
        return getFields(obj, new Class<?>[] {}, (Class<?>) null);
    }
    public static List<Field> getFieldsDeep(Object obj) {
        return getFieldsRegress(obj, new Class<?>[] { }, Object.class);
    }
    public static List<Field> getFields(Object obj, Class<?>... stopTypes) {
        return getFieldsRegress(obj, stopTypes, Object.class);
    }
    public static List<Field> getFields(Object obj, Class<?>[] filterTypes, Class<?>... stopTypes) {
        return getFields(obj, getFieldsDeep(obj.getClass(), stopTypes), filterTypes, f -> !isStatic(f.getModifiers()));
    }
    public static List<Field> getFieldsRegress(Object obj, Class<?>[] filterTypes, Class<?>... stopTypes) {
        return getFields(obj, getFieldsRegress(obj.getClass(), stopTypes), filterTypes, f -> !isStatic(f.getModifiers()));
    }
    public static List<Field> getFieldsExact(Class<?> cl) {
        return getTypeFields(cl);
    }
    public static List<Field> getFieldsExact(Class<?> cl, JFunc1<Field, Boolean> filter) {
        return filter(getFieldsExact(cl), filter);
    }
    public static List<Field> getFieldsExact(Class<?> cl, Class<?>... stopTypes) {
        return getFieldsExact(cl, f -> any(stopTypes, stopType -> f.getType() == stopType));
    }
    public static List<Field> getFieldsExact(Class<?> cl, Class<?> stopType) {
        return getFieldsExact(cl, f -> f.getType() == stopType);
    }
    public static List<Field> getFieldsExact(Object obj, Class<?>... stopTypes) {
        return getFieldsExact(obj.getClass(), stopTypes);
    }
    public static List<Field> getFields(List<Field> fields, Class<?>[] filterTypes, Function<Field, Boolean> filter) {
        return getFields(null, fields, filterTypes, filter);
    }
    public static List<Field> getFieldsInterfaceOf(Object obj, Class<?>... filterTypes) {
        return getFields(obj, getTypeFields(obj.getClass()), filterTypes, f -> true);
    }
    public static List<Field> getFields(Object obj, List<Field> fields, Class<?>[] filterTypes, Function<Field, Boolean> filter) {
        if (obj == null) {
            return new ArrayList<>();
        }
        List<Field> result = new ArrayList<>();
        try {
            for (Field field : fields) {
                if (invokeBoolean(filter, field)) {
                    Object value = getValueField(field, obj);
                    if (value != null) {
                        if (isExpectedClass(value, filterTypes))
                            result.add(field);
                    } else if (isExpectedClass(field, filterTypes))
                        result.add(field);
                }
            }
            return result;
        } catch (Exception ex) {
            throw new RuntimeException(format("Failed to get Fields from '%s'; fields: %s; filterTypes: %s", obj.getClass().getSimpleName(), PrintUtils.print(fields, Field::getName),
                    PrintUtils.print(asList(filterTypes), Class::getSimpleName)));
        }
    }
    public static List<Field> getFieldsRegress(Class<?> type, Class<?>... stopTypes) {
        if (stopTypes == null || stopTypes.length == 0)
            return getTypeFields(type);
        return recursion(type, t -> !t.equals(Object.class), stopTypes.length == 1 && stopTypes[0] == Object.class
            ? ReflectionUtils::getFieldsDeep3
            : t -> getFieldsDeep2(t, stopTypes));
    }
    public static List<Field> recursion(Class<?> objType,
            JFunc1<Class<?>, Boolean> condition, JFunc1<Class<?>, List<Field>> func) {
        List<Field> fields = new ArrayList<>();
        while (condition.execute(objType)) {
            List<Field> fList = func.execute(objType);
            for (Field field : fList) {
                Field notUnique = first(fields, f -> f.getName().equals(field.getName()));
                if (notUnique != null)
                    fields.remove(notUnique);
                fields.add(field);
            }
            objType = objType.getSuperclass();
        }
        return fields;
    }
    public static List<Field> getFieldsDeep(Class<?> type, Class<?>... stopTypes) {
        if (stopTypes == null || stopTypes.length == 0)
            return getTypeFields(type);
        return stopTypes.length == 1 && stopTypes[0] == Object.class
            ? getFieldsDeep3(type)
            : getFieldsDeep2(type, stopTypes);
    }
    private static List<Field> getFieldsDeep3(Class<?> type) {
        if (type == Object.class)
            return new ArrayList<>();
        return new ArrayList<>(getTypeFields(type));
    }

    private static List<Field> getFieldsDeep2(Class<?> type, Class<?>[] stopTypes) {
        if (asList(stopTypes).contains(type) || type == Object.class)
            return new ArrayList<>();
        return new ArrayList<>(getTypeFields(type));
    }

    public static <T> T getFirstField(Object obj, Class<?>... types) {
        return (T) getValueField(first(getTypeFields(obj.getClass()), field -> isExpectedClass(field, types)), obj);
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

    public static Object stringToPrimitive(String str) {
        if (str.equalsIgnoreCase("true"))
            return true;
        if (str.equalsIgnoreCase("false"))
            return false;
        try { return Byte.parseByte(str); }
        catch (Exception ignore) { }
        try { return Short.parseShort(str); }
        catch (Exception ignore) { }
        try { return Byte.parseByte(str); }
        catch (Exception ignore) { }
        try { return Integer.parseInt(str); }
        catch (Exception ignore) { }
        try { return Long.parseLong(str); }
        catch (Exception ignore) { }
        try { return Float.parseFloat(str); }
        catch (Exception ignore) { }
        try { return Double.parseDouble(str); }
        catch (Exception ignore) { }
        return str;
    }
    public static Object convertStringToType(String value, Class<?> clazz) {
        if (clazz.isAssignableFrom(String.class) || value == null)
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
        throw new IllegalArgumentException("Can't parse field string " + value + ". Type [" + clazz + "] is unsupported");
    }
    public static Object convertStringToType(String value, Field field) {
        Class<?> clazz = field.getType();
        try {
            return convertStringToType(value, clazz);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Can't parse field " + field.getName() + ". Type [" + clazz + "] is unsupported");
        }
    }

    public static <T> Class<T> checkEntityIsNotNull(Class<T> entityClass) {
        if (entityClass == null)
            throw new IllegalArgumentException("Entity type was not specified");
        return entityClass;
    }

    public static <T> T newEntity(Class<T> entityClass) {
        try {
            return entityClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't instantiate " + entityClass.getSimpleName() +
                    ". You must have empty constructor to do this");
        }
    }
    private static List<Field> getTypeFields(Class<?> type) {
        Field[] fields = type.getDeclaredFields();
        return filter(fields, f -> !f.getName().contains("$"));
    }

    private static <T> T csInit(Constructor<?> cs, Object... params) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        cs.setAccessible(true);
        return (T) cs.newInstance(params);
    }
    public static <T> T create(Class<T> cs) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (cs == null)
            throw new RuntimeException("Can't init class. Class Type is null.");
        Constructor<?>[] constructors = cs.getDeclaredConstructors();
        Constructor<?> constructor = first(constructors, c -> c.getParameterCount() == 0);
        if (constructor != null)
            return csInit(constructor);
        throw new RuntimeException(format("%s has no empty constructors", cs.getSimpleName()));
    }
    public static <T> T create(Class<T> cs, Object... params) {
        if (cs == null)
            throw new RuntimeException("Can't init class. Class Type is null.");
        Constructor<?>[] constructors = cs.getDeclaredConstructors();
        List<Constructor<?>> listConst = filter(constructors, c -> c.getParameterCount() == params.length);
        if (isEmpty(listConst))
            throw new RuntimeException(format("%s has no constructor with %s params", cs.getSimpleName(), params.length));
        for(Constructor<?> cnst : listConst) {
            try {
                return csInit(cnst, params);
            } catch (Exception ignore) { }
        }
        throw new RuntimeException(format("%s has no appropriate constructors", cs.getSimpleName()));
    }

    public static boolean isList(Field f, JFunc1<Class<?>, Boolean> func) {
        try {
            return f.getType() == List.class && func.execute(getGenericType(f));
        } catch (Exception ex) { return false; }
    }
    public static boolean isList(Class<?> clazz, JFunc1<Class<?>, Boolean> func) {
        try {
            return clazz == List.class && func.execute(getGenericType(clazz));
        } catch (Exception ex) { return false; }
    }
    public static boolean isList(Field f, Class<?> type) {
        return isList(f, g -> g == type);
    }
    public static boolean isListOf(Field field, Class<?> type) {
        return isList(field, g -> isClass(g, type) || isInterface(g, type));
    }
    public static Type[] getGenericTypes(Field field) {
        try {
            return ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
        } catch (Exception ex) {
            throw new RuntimeException(format("'%s' is List but has no Generic types", field.getName()), ex);
        }
    }
    public static Class<?> getGenericType(Field field) {
        try {
            return (Class<?>)getGenericTypes(field)[0];
        } catch (Exception ex) {
            throw new RuntimeException(format("'%s' is List but has no Generic types", field.getName()), ex);
        }
    }
    public static Class<?> getGenericType(Class<?> clazz) {
        try {
            return (Class<?>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
        } catch (Exception ex) {
            throw new RuntimeException(format("'%s' is List but has no Generic type", clazz.getName()), ex);
        }
    }
}