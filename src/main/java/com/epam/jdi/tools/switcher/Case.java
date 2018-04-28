package com.epam.jdi.tools.switcher;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.func.JAction1;
import com.epam.jdi.tools.func.JFunc1;

public class Case<T> {
    JFunc1<T, Boolean> condition;
    JAction1<T> action;
    Case(JFunc1<T, Boolean> condition, JAction1<T> action) {
        this.condition = condition;
        this.action = action;
    }
}
