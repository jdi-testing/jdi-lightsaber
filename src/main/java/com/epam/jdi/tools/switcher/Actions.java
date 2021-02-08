package com.epam.jdi.tools.switcher;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Roman_Iovlev on 4/28/2018.
 */
public class Actions {
    public static <T> Case<T> Case(Boolean value, Consumer<T> action) {
        return new Case<>(t -> value, action);
    }
    public static <T> Case<T> Case(Function<T, Boolean> value, Consumer<T> action) {
        return new Case<>(value, action);
    }
    public static <T> Case<T> Value(T value, Consumer<T> action) {
        return new Case<>(t -> t.equals(value), action);
    }
    public static <T> Case<T> Default(Consumer<T> action) {
        return new Case<>(t -> true, action);
    }
    public static <T> Case<T> Else(Consumer<T> action) {
        return new Case<>(t -> true, action);
    }

}
