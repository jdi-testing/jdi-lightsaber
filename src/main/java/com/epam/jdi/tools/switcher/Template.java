package com.epam.jdi.tools.switcher;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.LinqUtils;
import com.epam.jdi.tools.func.JFunc1;

import java.util.List;

import static com.epam.jdi.tools.LinqUtils.*;
import static java.util.Arrays.*;

public class Template<T, R> {
    private final JFunc1<T, Boolean> template;
    private final JFunc1<T, R> result;
    Template(JFunc1<T, Boolean> template, JFunc1<T, R> result) {
        this.template = template;
        this.result = result;
    }
    public void of(List<T> pairs) {
        foreach(filter(pairs, template::execute), result::execute);
    }
    public void of(T... pairs) {
        of(asList(pairs));
    }
    public R get(List<T> pairs) {
        T first = LinqUtils.first(pairs, template::execute);
        return first != null
                ? result.execute(first)
                : null;
    }
    public R get(T... pairs) {
        return get(asList(pairs));
    }
}
