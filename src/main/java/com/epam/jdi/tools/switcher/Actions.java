package com.epam.jdi.tools.switcher;

import com.epam.jdi.tools.func.JAction1;
import com.epam.jdi.tools.func.JFunc1;

/**
 * Created by Roman_Iovlev on 4/28/2018.
 */
public class Actions {
    public static <T> Case<T> Case(Boolean value, JAction1<T> action) {
        return new Case<>(t -> value, action);
    }
    public static <T> Case<T> Case(JFunc1<T, Boolean> value, JAction1<T> action) {
        return new Case<>(value, action);
    }
    public static <T> Case<T> Value(T value, JAction1<T> action) {
        return new Case<>(t -> t.equals(value), action);
    }
    public static <T> Case<T> Default(JAction1<T> action) {
        return new Case<>(t -> true, action);
    }
    public static <T> Case<T> Else(JAction1<T> action) {
        return new Case<>(t -> true, action);
    }

}
