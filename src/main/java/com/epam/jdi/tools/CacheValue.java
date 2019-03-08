package com.epam.jdi.tools;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.func.JFunc;

import static java.lang.System.currentTimeMillis;

public class CacheValue<T> {
    private static ThreadLocal<Long> globalCache = new ThreadLocal<>();
    private static Long getGlobalCache() {
        if (globalCache.get() == null) {
            globalCache.set(0L);
        }
        return globalCache.get();
    }
    public static void reset() {
        globalCache.set(currentTimeMillis());
    }
    private long elementCache = 0;
    private T value;
    private boolean isFinal = false;
    private JFunc<T> getRule = () -> null;
    public CacheValue() { }
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
        if (elementCache < getGlobalCache() || value == null) {
            this.value = getRule.execute();
            elementCache = getGlobalCache();
        }
        return value;
    }
    public void useCache(boolean value) { elementCache = value ? 0 : -1; }
    public T setForce(T value) {
        if (isFinal) return value;
        elementCache = getGlobalCache();
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
    public boolean hasValue() { return isFinal || isUseCache() && value != null && elementCache == getGlobalCache(); }
    public boolean isUseCache() { return elementCache > -1; }
}
