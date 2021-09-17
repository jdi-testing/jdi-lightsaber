package com.jdiai.tools.func;

import java.util.function.Consumer;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

@FunctionalInterface
public interface JAction1<T> extends Consumer<T> {
    void invoke(T val) throws Exception;
    @Override
    default void accept(T val) {
        execute(val);
    }

    default void execute(T val) {
        try {
            invoke(val);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}