package com.epam.jdi.tools;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.func.JFunc;

import static java.lang.System.*;

public class CacheValue<T> {
    private static Safe<Long> globalCache = new Safe<>(0L);

    public static void reset() {
        globalCache.set(currentTimeMillis());
    }
    private long elementCache = 0;
    private T value;
    private boolean isFinal = false;
    private JFunc<T> getRule = () -> null;
    public CacheValue() { }
    public CacheValue<T> copy() {
        CacheValue<T> cv = new CacheValue<>();
        cv.elementCache = elementCache;
        cv.value = value;
        cv.isFinal = isFinal;
        cv.getRule = getRule;
        return cv;
    }
    public CacheValue(JFunc<T> getRule) { this.getRule = getRule; }
    public T get() {
        return get(getRule);
    }
    public T getForce() {
        reset();
        return get();
    }
    public T get(JFunc<T> defaultResult) {
        if (isFinal) return value;
        if (!isUseCache()) return defaultResult.execute();
        if (elementCache < globalCache.get() || value == null) {
            this.value = getRule.execute();
            elementCache = globalCache.get();
        }
        return value;
    }
    public void useCache(boolean value) { elementCache = value ? 0 : -1; }
    public T setForce(T value) {
        if (isFinal) return value;
        elementCache = globalCache.get();
        this.value = value;
        return value;
    }
    public T setFinal(T value) {
        this.value = value;
        isFinal = true;
        return value;
    }
    public T set(T value) {
        return isFinal || !isUseCache()
            ? value
            : setForce(value);
    }
    public void setRule(JFunc<T> getRule) { this.getRule = getRule; }
    public void clear() { if (!isFinal) value = null; }
    public boolean hasValue() { return isFinal || isUseCache() && value != null && elementCache == globalCache.get(); }
    public boolean isUseCache() { return elementCache > -1; }
}
