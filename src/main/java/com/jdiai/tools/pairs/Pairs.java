package com.jdiai.tools.pairs;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.jdiai.tools.PrintUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static com.jdiai.tools.PrintUtils.print;
import static java.util.Arrays.asList;

public class Pairs<TValue1, TValue2> extends ArrayList<Pair<TValue1, TValue2>> {
    // TODO update pairs to MapArray level
    // Tuple
    // Pull Tuple any get removes element
    public Pairs() { }

    public Pairs(Pair<TValue1, TValue2>... pairs) {
        if (pairs == null) return;
        addAll(asList(pairs));
    }
    public Pairs(List<Pair<TValue1, TValue2>> pairs) {
        if (pairs == null) return;
        addAll(pairs);
    }

    public Pairs(TValue1 value1, TValue2 value2, Collection<Pair<TValue1, TValue2>> pairs) {
        if (pairs != null)
            this.addAll(pairs);
        add(value1, value2);
    }

    public static <T, TValue1, TValue2> Pairs<TValue1, TValue2> toPairs(Iterable<T> list,
                Function<T, TValue1> selectorValue1, Function<T, TValue2> selectorValue2) {
        Pairs<TValue1, TValue2> pairs = new Pairs<>();
        for (T element : list)
            pairs.add(selectorValue1.apply(element), selectorValue2.apply(element));
        return pairs;
    }

    public Pairs<TValue1, TValue2> add(TValue1 value1, TValue2 value2) {
        this.add(Pair.$(value1, value2));
        return this;
    }

    public Pairs<TValue1, TValue2> add(Pair<TValue1, TValue2>... pairs) {
        this.addAll(asList(pairs));
        return this;
    }
    public Pairs<TValue1, TValue2> add(Pairs<TValue1, TValue2> pairs) {
        this.addAll(pairs);
        return this;
    }

    public void addNew(TValue1 value1, TValue2 value2) {
        clear();
        add(Pair.$(value1, value2));
    }

    public Pairs<TValue1, TValue2> subList(int from) {
        return new Pairs<>(subList(from, size()));
    }

    @Override
    public String toString() {
        return PrintUtils.print(this, pair -> pair.key + ":" + pair.value);
    }

    @Override
    public Pairs<TValue1, TValue2> clone() {
        return new Pairs<>(this);
    }

    public Pairs<TValue1, TValue2> copy() {
        return clone();
    }
}