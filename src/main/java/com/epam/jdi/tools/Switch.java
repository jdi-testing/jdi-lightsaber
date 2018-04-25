package com.epam.jdi.tools;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.func.JFunc1;

import static com.epam.jdi.tools.LinqUtils.filter;
import static com.epam.jdi.tools.LinqUtils.foreach;

public class Switch<T> {
    private final T value;
    Switch() { value = null; }
    Switch(T value) { this.value = value; }
    public void of(Case<T>... pairs) {
        foreach(filter(pairs,
            p -> p.condition.execute(value)),
            p -> p.action.execute(value));
    }
    public <R> R get(CaseR<T, R>... pairs) {
        CaseR<T,R> result = LinqUtils.first(pairs, p -> p.condition.execute(value));
        return result != null
            ? result.result.execute(value)
            : null;
    }
    public static <T,R> CaseR<T,R> Case(JFunc1<T, Boolean> value, JFunc1<T, R> result) {
        return new CaseR<>(value, result);
    }
    public static <T,R> CaseR<T,R> Case(JFunc1<T, Boolean> value, R result) {
        return new CaseR<>(value, t -> result);
    }
    public static <T,R> CaseR<T,R> Condition(Boolean value, JFunc1<T, R> result) {
        return new CaseR<>(t -> value, result);
    }
    public static <T,R> CaseR<T,R> Condition(Boolean value, R result) {
        return new CaseR<>(t -> value, t -> result);
    }
    public static <T,R> CaseR<T,R> Value(T value, JFunc1<T, R> result) {
        return new CaseR<>(t -> t.equals(value), result);
    }
    public static <T,R> CaseR<T,R> Value(T value, R result) {
        return new CaseR<>(t -> t.equals(value), t -> result);
    }
    public static <T,R> CaseR<T,R> Default(JFunc1<T, R> result) {
        return new CaseR<>(t -> true, result);
    }
    public static <T,R> CaseR<T,R> Default(R result) {
        return new CaseR<>(t -> true, t -> result);
    }
    public static <T,R> CaseR<T,R> Else(JFunc1<T, R> result) {
        return new CaseR<>(t -> true, result);
    }
    public static <T,R> CaseR<T,R> Else(R result) {
        return new CaseR<>(t -> true, t -> result);
    }
}
