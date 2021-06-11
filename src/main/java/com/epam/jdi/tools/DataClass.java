package com.epam.jdi.tools;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.map.MapArray;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.epam.jdi.tools.ReflectionUtils.getValueField;
import static java.lang.String.format;
import static java.lang.String.join;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

public class DataClass<T> implements Cloneable, ComparableData {
    public T set(Consumer<T> valueFunc) {
        T thisObj = (T) this;
        valueFunc.accept(thisObj);
        return thisObj;
    }
    public Map<String, Object> fieldsAsMap() {
        Map<String, Object> map = new HashMap<>();
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            map.put(field.getName(), getValueField(field, this));
        }
        return map;
    }
    public MapArray<String, Object> fields() {
        Field[] fields = getClass().getDeclaredFields();
        return new MapArray<>(fields, Field::getName, f -> getValueField(f, this));
    }

    @Override
    public String toString() {
        return PrintUtils.printFields(this);
    }

    public String compareTo(DataClass<?> article) {
        List<String> inequality = new ArrayList<>();
        Map<String, Object> toCompare = article.fieldsAsMap();
        for (Map.Entry<String, Object> field : fieldsAsMap().entrySet()) {
            Object compareValue = toCompare.get(field.getKey());
            if (compareValue == null) {
                if (field.getValue() != null) {
                    inequality.add(format("Field '%s' value should be '%s' but 'null'",
                        field.getKey(), field.getValue()));
                }
            } else if (!compareValue.equals(field.getValue())) {
                inequality.add(format("Field '%s' value should be '%s' but '%s'",
                    field.getKey(), field.getValue(), compareValue));
            }
        }
        return isNotEmpty(inequality) ? join("\n", inequality) : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        try {
            Field[] otherFields = o.getClass().getDeclaredFields();
            for (Field f : getClass().getDeclaredFields()) {
                Field fOther = LinqUtils.first(otherFields, fo -> fo.getName().equals(f.getName()));
                if (getValueField(f, this) == null && fOther.get(o) == null)
                    continue;
                if (getValueField(f, this) != null && fOther.get(o) == null ||
                    getValueField(f, this) == null && fOther.get(o) != null ||
                    !getValueField(f, this).equals(fOther.get(o)))
                    return false;
                }
        }
        catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (Field f : getClass().getDeclaredFields())
            try {
                Object value = getValueField(f, this);
                result += 31 * result + (value != null ? value.hashCode() : 0);
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage());
            }
        return result;
    }
}
