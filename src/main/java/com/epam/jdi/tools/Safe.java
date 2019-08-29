package com.epam.jdi.tools;

import com.epam.jdi.tools.func.JFunc;
import com.epam.jdi.tools.func.JFunc1;

public class Safe<T> extends ThreadLocal<T> {
    private JFunc<T> defult;
    public Safe() { }
    public Safe(JFunc<T> func) { defult = func; }
    public Safe(T value) { defult = () -> value; }
    @Override
    public T get() {
        if (super.get() == null)
            set(defult.execute());
        return super.get();
    }
    public void update(JFunc1<T, T> func) {
        set(func.execute(get()));
    }
    public void reset() { set(defult.execute()); }
    public T getDefault() { return defult.execute(); }
}
