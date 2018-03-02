package com.epam.jdi.tools;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.func.JFunc;

import static java.lang.System.currentTimeMillis;

public class CacheValue<T> {
    private static long globalCache;
    public static void reset() { globalCache = currentTimeMillis(); }
    public long elementCache = 0;
    private T value;
    private JFunc<T> getRule;
    public CacheValue(JFunc<T> getRule) { this.getRule = getRule; }
    public T get() {
        return get(() -> null);
    }
    public T get(JFunc<T> defaultResult) {
        if (!isUseCache()) return defaultResult.execute();
        if (elementCache < globalCache || value == null) {
            this.value = getRule.execute();
            elementCache = globalCache;
        }
        return value;
    }
    public void useCache(boolean value) { elementCache = value ? 0 : -1; }
    public T setForce(T value) {
        elementCache = globalCache;
        this.value = value;
        return value;
    }
    public T set(T value) {
        return isUseCache()
            ? setForce(value)
            : value;
    }
    public void setRule(JFunc<T> getRule) { this.getRule = getRule; }
    public void clear() { value = null; }
    public boolean hasValue() { return isUseCache() && value != null && elementCache == globalCache;}
    public boolean isUseCache() { return elementCache > -1; }
}
