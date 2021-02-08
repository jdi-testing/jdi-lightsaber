package com.epam.jdi.tools.switcher;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.LinqUtils;

import static com.epam.jdi.tools.LinqUtils.filter;
import static com.epam.jdi.tools.LinqUtils.foreach;

public class Switch<T> {

    private final T value;
    Switch() { value = null; }
    Switch(T value) { this.value = value; }
    public void of(Case<T>... pairs) {
        foreach(filter(pairs,
            p -> p.condition.apply(value)),
            p -> p.action.accept(value));
    }
    public <R> R get(CaseR<T, R>... pairs) {
        CaseR<T,R> result = LinqUtils.first(pairs, p -> p.condition.apply(value));
        return result != null
            ? result.result.apply(value)
            : null;
    }
}
