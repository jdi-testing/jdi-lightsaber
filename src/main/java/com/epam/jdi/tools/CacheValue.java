package com.epam.jdi.tools;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.func.JFunc;

import static java.lang.System.currentTimeMillis;

public class CacheValue<T> {
    private static Safe<Long> globalCache = new Safe<>(0L);

    public static void reset() {
        globalCache.set(currentTimeMillis());
    }
    private Safe<Long> elementCache = new Safe<>(() -> 0L);
    private Safe<T> value = new Safe<>(() -> null);
    private T finalValue = null;
    private JFunc<T> getRule = () -> null;
    public CacheValue() { }
    public CacheValue<T> copy() {
        CacheValue<T> cv = new CacheValue<>();
        cv.elementCache = new Safe<>(() -> elementCache.get());
        cv.value = new Safe<>(() -> value.get());
        cv.finalValue = finalValue;
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
        if (finalValue != null)
            return finalValue;
        if (!isUseCache())
            return defaultResult.execute();
        if (elementCache.get() < globalCache.get() || value.get() == null) {
            this.value.set(getRule.execute());
            elementCache.set(globalCache.get());
        }
        return value.get();
    }
    public void useCache(boolean value) { elementCache.set(value ? 0L : -1L); }
    public T setForce(T value) {
        if (finalValue != null)
            return finalValue;
        elementCache.set(globalCache.get());
        this.value.set(value);
        return value;
    }
    public T setFinal(T value) {
        finalValue = value;
        return value;
    }
    public T set(T value) {
        if (finalValue != null)
            return finalValue;
        return !isUseCache() ? value : setForce(value);
    }
    public void setRule(JFunc<T> getRule) {
        this.getRule = getRule;
    }
    public void clear() {
        value.set(null);
    }
    public boolean hasValue() {
        return finalValue != null || isUseCache() && value.get() != null && elementCache.get().equals(globalCache.get());
    }
    public boolean isUseCache() {
        return elementCache.get() > -1;
    }
}
