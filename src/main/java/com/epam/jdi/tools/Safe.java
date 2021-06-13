package com.epam.jdi.tools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.Thread.currentThread;

public class Safe<T> extends ThreadLocal<T> {
    private Supplier<T> DEFAULT;
    public Safe() { this(() -> null); }
    public Safe(Supplier<T> func) { DEFAULT = func; }
    public Safe(T value) { this(() -> value); }
    Map<Long, T> threadValues = new ConcurrentHashMap<>();
    @Override
    public T get() {
        long threadId = threadId();
        if (hasValue()) {
            return threadValues.get(threadId);
        }
        T value;
        if (threadValues.size() == 1 && threadValues.containsKey(1L)) {
            value = threadValues.get(1L);
            threadValues.clear();
        } else {
            value = DEFAULT.get();
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
    public boolean hasValue() {
        return threadValues.containsKey(threadId());
    }
    public void reset() { set(DEFAULT.get()); }
    public T getDefault() { return DEFAULT.get(); }
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
