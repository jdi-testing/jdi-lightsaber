package com.epam.jdi.tools.switcher;

import java.util.function.Function;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

public class CaseR<T, R> {
    Function<T, Boolean> condition;
    Function<T, R> result;
    CaseR(Function<T, Boolean> condition, Function<T, R> result) {
        this.condition = condition;
        this.result = result;
    }
}
