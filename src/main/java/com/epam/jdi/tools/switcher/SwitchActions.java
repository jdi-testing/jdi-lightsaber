package com.epam.jdi.tools.switcher;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.func.JFunc1;

public class SwitchActions {
    public static <T> Switch<T> Switch() {
        return new Switch<>();
    }
    public static <T> Switch<T> Switch(T value) {
        return new Switch<>(value);
    }
    public static <T, R> Template<T, R> Template(JFunc1<T, Boolean> template, JFunc1<T, R> result) {
        return new Template<>(template, result);
    }
    // Functions
    public static <T,R> CaseR<T,R> Value(T value, JFunc1<T,R> result) {
        return new CaseR<>(t -> t.equals(value), result);
    }
    public static <T,R> CaseR<T,R> Case(Boolean value, JFunc1<T, R> result) {
        return new CaseR<>(t -> value, result);
    }
    public static <T,R> CaseR<T,R> Case(JFunc1<T, Boolean> value, JFunc1<T, R> result) {
        return new CaseR<>(value, result);
    }
    public static <T,R> CaseR<T,R> Default(JFunc1<T, R> result) {
        return new CaseR<>(t -> true, result);
    }
    public static <T,R> CaseR<T,R> Else(JFunc1<T, R> result) {
        return new CaseR<>(t -> true, result);
    }

    // Result
    public static <T,R> CaseR<T,R> Value(T value, R result) {
        return new CaseR<>(t -> t.equals(value), t -> result);
    }
    public static <T,R> CaseR<T,R> Case(Boolean value, R result) {
        return new CaseR<>(t -> value, t -> result);
    }
    public static <T,R> CaseR<T,R> Case(JFunc1<T, Boolean> value, R result) {
        return new CaseR<>(value, t -> result);
    }

    public static <T,R> CaseR<T,R> Default(R result) {
        return new CaseR<>(t -> true, t -> result);
    }
    public static <T,R> CaseR<T,R> Else(R result) {
        return new CaseR<>(t -> true, t -> result);
    }
}
