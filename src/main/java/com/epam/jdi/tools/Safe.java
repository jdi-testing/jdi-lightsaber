package com.epam.jdi.tools;

import com.epam.jdi.tools.func.JFunc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.lang.Thread.currentThread;

public class Safe<T> extends ThreadLocal<T> {
    private JFunc<T> DEFAULT;
    public Safe() { this(() -> null); }
    public Safe(JFunc<T> func) { DEFAULT = func; }
    public Safe(T value) { this(() -> value); }
    Map<Long, T> threadValues = new ConcurrentHashMap<>();
    @Override
    public T get() {
        long threadId = threadId();
        if (threadValues.containsKey(threadId)) {
            return threadValues.get(threadId);
        }
        T value;
        if (threadValues.size() == 1 && threadValues.containsKey(1L)) {
            value = threadValues.get(1L);
            threadValues.clear();
        } else {
            value = DEFAULT.execute();
        }
        update(threadId, value);
        return value;
    }
    @Override
    public void set(T value) {
        update(threadId(), value);
    }
    public void update(Function<T, T> func) {
        set(func.apply(get()));
    }
    public void reset() { set(DEFAULT.execute()); }
    public T getDefault() { return DEFAULT.execute(); }
    private long threadId() {
        return currentThread().getId();
    }

    private void update(long threadId, T value) {
        threadValues.compute(threadId,  (k,v) -> value);
    }
    @Override
    public String toString() {
        return threadValues.keySet().toString();
    }
}
