package com.jdiai.tools.func;

import java.util.function.BiConsumer;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

@FunctionalInterface
public interface JAction2<T1, T2> extends BiConsumer<T1, T2> {
    void invoke(T1 val1, T2 val2) throws Exception;
    @Override
    default void accept(T1 val1, T2 val2) {
        execute(val1, val2);
    }
    default void execute(T1 val1, T2 val2) {
        try {
            invoke(val1, val2);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}