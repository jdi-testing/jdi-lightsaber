package com.jdiai.tools.switcher;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.jdiai.tools.LinqUtils;

import java.util.List;
import java.util.function.Function;

import static com.jdiai.tools.LinqUtils.filter;
import static com.jdiai.tools.LinqUtils.foreach;
import static java.util.Arrays.asList;

public class Template<T, R> {
    private final Function<T, Boolean> template;
    private final Function<T, R> result;
    Template(Function<T, Boolean> template, Function<T, R> result) {
        this.template = template;
        this.result = result;
    }
    public void of(List<T> pairs) {
        LinqUtils.foreach(LinqUtils.filter(pairs, template::apply), result::apply);
    }
    public void of(T... pairs) {
        of(asList(pairs));
    }
    public R get(List<T> pairs) {
        T first = LinqUtils.first(pairs, template::apply);
        return first != null
                ? result.apply(first)
                : null;
    }
    public R get(T... pairs) {
        return get(asList(pairs));
    }
}
