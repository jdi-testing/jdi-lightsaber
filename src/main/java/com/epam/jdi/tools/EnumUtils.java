package com.epam.jdi.tools;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.epam.jdi.tools.LinqUtils.map;
import static com.epam.jdi.tools.LinqUtils.toStringArray;
import static com.epam.jdi.tools.TryCatchUtil.tryGetResult;
import static java.util.Arrays.asList;

public final class EnumUtils {
    private EnumUtils() {
    }

    public static String[] getEnumValues(Enum... values) {
        String[] result = new String[values.length];
        for (int i=0; i < values.length; i++)
            result[i] = getEnumValue(values[i]);
        return result;
    }
    public static String getEnumValue(Enum enumWithValue) {
        Class<?> type = enumWithValue.getClass();
        Field[] fields = type.getDeclaredFields();
        Field field;
        switch (fields.length) {
            case 0:
                return enumWithValue.toString();
            case 1:
                field = fields[0];
                break;
            default:
                try {
                    field = type.getField("value");
                } catch (NoSuchFieldException ex) {
                    return enumWithValue.toString();
                }
                break;
        }
        return tryGetResult(() -> field.get(enumWithValue).toString());
    }

    public static <T extends Enum> List<T> getAllEnumValues(Class<T> enumValue) {
        return asList(getAllEnumValuesAsArray(enumValue));
    }

    public static <T extends Enum> T[] getAllEnumValuesAsArray(Class<T> enumValue) {
        return enumValue.getEnumConstants();
    }

    public static <T extends Enum> List<String> getAllEnumNames(Class<T> enumValue) {
        return LinqUtils.select(getAllEnumValuesAsArray(enumValue), EnumUtils::getEnumValue);
    }

    public static <T extends Enum> String[] getAllEnumNamesAsArray(Class<T> enumValue) {
        return LinqUtils.toStringArray(getAllEnumNames(enumValue));
    }
}