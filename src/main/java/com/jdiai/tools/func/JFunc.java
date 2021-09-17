package com.jdiai.tools.func;

import java.util.function.Supplier;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

@FunctionalInterface
public interface JFunc<R> extends Supplier<R> {
    R invoke() throws Exception;
    @Override
    default R get() {
        return execute();
    }
    default R execute() {
        try {
            return invoke();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}