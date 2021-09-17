package com.jdiai.tools.func;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

@FunctionalInterface
public interface JAction6<T1, T2, T3, T4, T5, T6> {
    void invoke(T1 val1, T2 val2, T3 val3, T4 val4, T5 val5, T6 val6) throws Exception;

    default void execute(T1 val1, T2 val2, T3 val3, T4 val4, T5 val5,
                         T6 val6) {
        try {
            invoke(val1, val2, val3, val4, val5, val6);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}