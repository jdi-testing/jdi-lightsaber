package com.epam.jdi.tools;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import java.lang.reflect.Field;
import java.util.List;

import static com.epam.jdi.tools.LinqUtils.first;
import static com.epam.jdi.tools.ReflectionUtils.getValueField;
import static java.util.Arrays.asList;

public final class EnumUtils {
    private EnumUtils() {
    }

    public static String[] getEnumValues(Enum<?>... values) {
        String[] result = new String[values.length];
        for (int i=0; i < values.length; i++)
            result[i] = getEnumValue(values[i]);
        return result;
    }
    public static String getEnumValue(Enum<?> enumWithValue) {
        Class<?> type = enumWithValue.getClass();
        Field[] fields = type.getDeclaredFields();
        try {
            switch (fields.length) {
                case 0:
                    return enumWithValue.toString();
                case 1:
                    return fields[0].get(enumWithValue).toString();
                default:
                    return getEnumValueFromValueField(type, enumWithValue);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to get Enum value: " + enumWithValue, ex);
        }
    }
    private static String getEnumValueFromValueField(Class<?> type, Enum<?> enumWithValue) {
        Field field;
        try {
            field = type.getDeclaredField("value");
            return getValueField(field, enumWithValue).toString();
        } catch (NoSuchFieldException ex) {
            return enumWithValue.toString();
        }
    }
    public static <T extends Enum<?>> T getEnumValueByName(Class<T> cl, String expectedValueName, T defaultValue) {
        T firstType = first(getAllEnumValues(cl), t -> getType(t, expectedValueName));
        return firstType != null ? firstType : defaultValue;
    }

    private static boolean getType(Object enumType, String type) {
        return enumType.toString().trim().replaceAll("[^a-z]", "")
                .equalsIgnoreCase(type.trim().replaceAll("[^a-z]", ""));
    }

    public static <T extends Enum<?>> List<T> getAllEnumValues(Class<T> enumValue) {
        return asList(getAllEnumValuesAsArray(enumValue));
    }

    public static <T extends Enum<?>> T[] getAllEnumValuesAsArray(Class<T> enumValue) {
        return enumValue.getEnumConstants();
    }

    public static <T extends Enum<?>> List<String> getAllEnumNames(Class<T> enumValue) {
        return LinqUtils.select(getAllEnumValuesAsArray(enumValue), EnumUtils::getEnumValue);
    }

    public static <T extends Enum<?>> String[] getAllEnumNamesAsArray(Class<T> enumValue) {
        return LinqUtils.toStringArray(getAllEnumNames(enumValue));
    }
}