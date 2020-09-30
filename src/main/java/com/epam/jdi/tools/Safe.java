package com.epam.jdi.tools;

import com.epam.jdi.tools.func.JFunc;
import com.epam.jdi.tools.func.JFunc1;
import com.epam.jdi.tools.map.MapArray;

import static java.lang.Thread.currentThread;

public class Safe<T> extends ThreadLocal<T> {
    private JFunc<T> defult;
    public Safe() { }
    public Safe(JFunc<T> func) { defult = func; }
    public Safe(T value) { defult = () -> value; }
    MapArray<Long, T> threadValues = new MapArray<>();
    @Override
    public T get() {
        long threadId = threadId();
        if (threadValues.has(threadId)) {
            return threadValues.get(threadId);
        }
        T value = defult.execute();
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
    public void reset() { set(defult.execute()); }
    public T getDefault() { return defult.execute(); }
    private long threadId() {
        return currentThread().getId();
    }

}
