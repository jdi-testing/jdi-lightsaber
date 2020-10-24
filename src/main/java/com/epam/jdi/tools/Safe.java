package com.epam.jdi.tools;

import com.epam.jdi.tools.func.JFunc;
import com.epam.jdi.tools.func.JFunc1;
import com.epam.jdi.tools.map.MapArray;

import static java.lang.Thread.currentThread;

public class Safe<T> extends ThreadLocal<T> {
    private JFunc<T> DEFAULT;
    public Safe() { this(() -> null); }
    public Safe(JFunc<T> func) { DEFAULT = func; }
    public Safe(T value) { this(() -> value); }
    MapArray<Long, T> threadValues = new MapArray<>();
    @Override
    public T get() {
        long threadId = threadId();
        if (threadValues.has(threadId)) {
            return threadValues.get(threadId);
        }
        T value = DEFAULT.execute();
        threadValues.update(threadId, value);
        return value;
    }
    @Override
    public void set(T value) {
        threadValues.update(threadId(), value);
    }
    public void update(JFunc1<T, T> func) {
        set(func.execute(get()));
    }
    public void reset() { set(DEFAULT.execute()); }
    public T getDefault() { return DEFAULT.execute(); }
    private long threadId() {
        return currentThread().getId();
    }

    // @Override
    // public String toString() {
    //     return threadValues.toString();
    // }
}
