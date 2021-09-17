package com.jdiai.tools.switcher;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import java.util.function.Consumer;
import java.util.function.Function;

public class Case<T> {
    Function<T, Boolean> condition;
    Consumer<T> action;
    Case(Function<T, Boolean> condition, Consumer<T> action) {
        this.condition = condition;
        this.action = action;
    }
}
