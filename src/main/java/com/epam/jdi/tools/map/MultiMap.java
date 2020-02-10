package com.epam.jdi.tools.map;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.LinqUtils;
import com.epam.jdi.tools.func.JAction1;
import com.epam.jdi.tools.func.JAction2;
import com.epam.jdi.tools.func.JFunc1;
import com.epam.jdi.tools.func.JFunc2;
import com.epam.jdi.tools.pairs.Pair;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.epam.jdi.tools.LinqUtils.listCopy;
import static com.epam.jdi.tools.LinqUtils.listCopyUntil;
import static com.epam.jdi.tools.PrintUtils.print;
import static com.epam.jdi.tools.TryCatchUtil.throwRuntimeException;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public class MultiMap<K, V> implements Collection<Pair<K, V>>, Cloneable {
    public List<Pair<K, V>> pairs;

    public static <K, V> MultiMap<K, V> multiMap(Pair<K, V>... pairs) {
        return new MultiMap<>(pairs);
    }
    public static <V> MultiMap<Integer, V> multiMap(List<V> list) {
        return new MultiMap<>(list.size(), i -> i, list::get);
    }

    public MultiMap() {
        pairs = new ArrayList<>();
    }

    public MultiMap(K key, V value) {
        this();
        add(key, value);
    }
    public MultiMap(Pair<K, V>... pairs) {
        this(asList(pairs));
    }
    public MultiMap(List<Pair<K, V>> pairs) {
        this();
        try {
            for (Pair<K, V> pair : pairs)
                add(pair.key, pair.value);
        } catch (Exception ex) {
            throw new RuntimeException(format("Can't create MapArray. Exception: %s", ex.getMessage()));
        }
    }

    public MultiMap(Collection<K> collection, JFunc1<K, V> value) {
        this(collection, k -> k, value::execute);
    }
    public <T> MultiMap(Collection<T> collection, JFunc1<T, K> keyFunc, JFunc1<T, V> valueFunc) {
        this();
        try {
            for (T t : collection)
                add(keyFunc.invoke(t), valueFunc.invoke(t));
        } catch (Exception ex) {
            throw new RuntimeException(format("Can't create MapArray. Exception: %s", ex.getMessage()));
        }
    }

    public MultiMap(K[] array, JFunc1<K, V> value) {
        this(asList(array), value);
    }
    public <T> MultiMap(T[] array, JFunc1<T, K> key, JFunc1<T, V> value) {
        this(asList(array), key, value);
    }

    public MultiMap(int count, JFunc1<Integer, K> keyFunc, JFunc1<Integer, V> value) {
        this();
        try {
            for (int i = 0; i < count; i++)
                add(keyFunc.invoke(i), value.invoke(i));
        } catch (Exception ex) {
            throw new RuntimeException(format("Can't create MapArray. Exception: %s", ex.getMessage()));
        }
    }
    public MultiMap(int count, JFunc1<Integer, Pair<K, V>> pairFunc) {
        this();
        try {
            for (int i = 0; i < count; i++) {
                Pair<K, V> pair = pairFunc.invoke(i);
                add(pair.key, pair.value);
            }
        } catch (Exception ex) {
            throw new RuntimeException(format("Can't create MapArray. Exception: %s", ex.getMessage()));
        }
    }

    public MultiMap(MultiMap<K, V> mapArray) {
        this();
        addAll(new ArrayList<>(mapArray));
    }
    public MultiMap(MapArray<K, V> mapArray) {
        this();
        addAll(new ArrayList<>(mapArray));
    }
    public MultiMap(Map<K, V> map) {
        this();
        for (Entry<K, V> entry : map.entrySet())
            add(entry.getKey(), entry.getValue());
    }

    public MultiMap(Object[][] objects) {
        this();
        add(objects);
    }
    public MultiMap(List<K> keys, List<V> values) {
        this();
        assert keys != null && values != null;
        if (keys.size() != values.size())
            throw new RuntimeException("keys and values has different count ");
        for (int i = 0; i < keys.size(); i++)
            add(keys.get(i), values.get(i));
    }
    public MultiMap(K[] keys, V[] values) {
        this(asList(keys), asList(values));
    }

    public static <T> MultiMap<Integer, T> toMultiMap(Collection<T> collection) {
        MultiMap<Integer, T> multiMap = new MultiMap<>();
        int i = 0;
        for (T t : collection)
            multiMap.add(i++, t);
        return multiMap;
    }

    public static <Value> MultiMap<Integer, Value> toMultiMap(int count, JFunc1<Integer, Value> valueFunc) {
        MultiMap<Integer, Value> multiMap = new MultiMap<>();
        try {
            for (int i = 0; i < count; i++)
                multiMap.add(i, valueFunc.invoke(i));
        } catch (Exception ex) {
            throw new RuntimeException(format("Can't get MapArray. Exception: %s", ex.getMessage())); }
        return multiMap;
    }
    public static <T> MultiMap<Integer, T> toMultiMap(T[] array) {
        return toMultiMap(asList(array));
    }

    public static <Key, Value> MultiMap<Key, Value> toMultiMap(Map<Key, Value> map) {
        MultiMap<Key, Value> mapArray = new MultiMap<>();
        for (Entry<Key, Value> e : map.entrySet())
            mapArray.add(e.getKey(), e.getValue());
        return mapArray;
    }

    public static <Key, Value> MultiMap<Key, Value> toMultiMap(MapArray<Key, Value> map) {
        MultiMap<Key, Value> mapArray = new MultiMap<>();
        for (Pair<Key, Value> e : map)
            mapArray.add(e.key, e.value);
        return mapArray;
    }
    public <KResult, VResult> MultiMap<KResult, VResult> toMultiMap(
            JFunc2<K, V, KResult> key, JFunc2<K, V, VResult> value) {
        MultiMap<KResult, VResult> result = new MultiMap<>();
        result.ignoreKeyCase = ignoreKeyCase;
        try {
            for (Pair<K, V> pair : pairs)
                result.add(key.invoke(pair.key, pair.value), value.invoke(pair.key, pair.value));
        } catch (Exception ex) {
            throw new RuntimeException(format("Can't convert toMap. Exception: %s", ex.getMessage())); }
        return result;
    }

    public <VResult> MultiMap<K, VResult> toMultiMap(JFunc1<V, VResult> value) {
        MultiMap<K, VResult> result = new MultiMap<>();
        result.ignoreKeyCase = ignoreKeyCase;
        try {
        for (Pair<K, V> pair : pairs)
            result.add(pair.key, value.invoke(pair.value));
        return result;
        } catch (Exception ex) {
            throw new RuntimeException(format("Can't convert toMap. Exception: %s", ex.getMessage())); }
    }

    public Map<K, V> toMap() {
        return toMap(v -> v);
    }
    public <VResult> Map<K, VResult> toMap(JFunc1<V, VResult> value) {
        return toMap((k, v) -> k, (k,v) -> value.invoke(v));
    }
    public <KResult, VResult> Map<KResult, VResult> toMap(
            JFunc2<K, V, KResult> key, JFunc2<K, V, VResult> value) {
        Map<KResult, VResult> result = new HashMap<>();
        try {
            for (Pair<K, V> pair : pairs)
                result.put(key.invoke(pair.key, pair.value),
                        value.invoke(pair.key, pair.value));
        } catch (Exception ex) {
            throw new RuntimeException(format("Can't convert toMap. Exception: %s", ex.getMessage()));
        }
        return result;
    }

    public MultiMap<K, V> add(K key, V value) {
        pairs.add(new Pair<>(key, value));
        return this;
    }

    public void add(Object[][] pairs) {
        for (Object[] pair : pairs)
            if (pair.length == 2)
                add(cast(pair[0]), cast(pair[1]));
    }

    private <R> R cast(Object obj) {
        try {
            return (R) obj;
        } catch (ClassCastException ex) {
            throw new ClassCastException(format("Can't cast element '%s' in MapArray. Exception: %s", obj, ex.getMessage()));
        }
    }

    public boolean has(K key) {
        return keys().contains(key);
    }

    public MultiMap<K, V> addFirst(K key, V value) {
        List<Pair<K, V>> result = new CopyOnWriteArrayList<>();
        result.add(new Pair<>(key, value));
        result.addAll(pairs);
        pairs = result;
        return this;
    }
    private boolean ignoreKeyCase = false;
    public MultiMap<K, V> ignoreKeyCase() { ignoreKeyCase = true; return this; }
    protected boolean keysEqual(K key1, K key2) {
        return key1.getClass() == String.class && ignoreKeyCase
            ? ((String) key1).equalsIgnoreCase((String) key2)
            : key1.equals(key2);
    }
    public int getIndex(K key) {
        for (int i = 0; i < keys().size(); i++)
            if (keysEqual(keys().get(i), key))
                return i;
        return -1;
    }
    public V get(K key) {
        Pair<K, V> first = null;
        try {
            first = LinqUtils.first(pairs, pair -> keysEqual(pair.key, key));
        } catch (Exception ignore) { }
        return (first != null) ? first.value : null;
    }
    public List<V> getList(K key) {
        List<V> result = null;
        try {
            result = LinqUtils.ifSelect(pairs,
                pair -> keysEqual(pair.key, key),
                pair -> pair.value);
        } catch (Exception ignore) { }
        return (result != null) ? result : new ArrayList<>();
    }

    public Pair<K, V> get(int i) {
        int index = i >= 0 ? i : pairs.size() + i;
        return index >= 0 && index < pairs.size()
            ? pairs.get(index)
            : null;
    }

    public K key(int index) {
        return get(index).key;
    }

    public V value(int index) {
        return get(index).value;
    }

    public List<K> keys() {
        return LinqUtils.map(pairs, pair -> pair.key);
    }
    public List<K> uniqueKeys() {
        List<K> keys = new ArrayList<>();
        for (Pair<K,V> pair : pairs)
            if (!keys.contains(pair.key))
                keys.add(pair.key);
        return keys;
    }

    public List<V> values() {
        return LinqUtils.map(pairs, pair -> pair.value);
    }
    public List<V> uniqueValues() {
        List<V> values = new ArrayList<>();
        for (Pair<K,V> pair : pairs)
            if (!values.contains(pair.value))
                values.add(pair.value);
        return values;
    }
    public List<V> values(JFunc1<V, Boolean> condition) {
        return LinqUtils.filter(values(), condition);
    }

    public int size() {
        return pairs.size();
    }

    public int count() {
        return size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }
    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public boolean any() {
        return size() > 0;
    }
    public boolean any(JFunc1<V, Boolean> func) {
        return LinqUtils.any(this.values(), func);
    }
    public boolean all(JFunc1<V, Boolean> func) {
        return LinqUtils.all(this.values(), func);
    }
    public Pair<K, V> first() {
        return get(0);
    }

    public Pair<K, V> last() {
        return get(-1);
    }

    public MultiMap<K, V> revert() {
        List<Pair<K, V>> result = new CopyOnWriteArrayList<>();
        for (int i = size() - 1; i >= 0; i--)
            result.add(get(i));
        pairs = result;
        return this;
    }

    public boolean contains(Object o) {
        return values().contains(o);
    }

    public Iterator<Pair<K, V>> iterator() {
        return pairs.iterator();
    }

    public Object[] toArray() {
        return pairs.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return pairs.toArray(a);
    }

    public boolean add(Pair<K, V> kv) {
        return pairs.add(kv);
    }

    public boolean remove(Object o) {
        boolean isRemoved = false;
        for (Object kv : pairs)
            if (kv.equals(o)) {
                pairs.remove(kv);
                isRemoved = true;
            }
        return isRemoved;
    }

    public void removeByKey(K key) {
        pairs.removeAll(LinqUtils.where(pairs,
            p -> keysEqual(p.key, key)));
    }
    public void removeByValue(V value) {
        pairs.removeAll(LinqUtils.where(pairs,
                p -> p.value.equals(value)));
    }
    public Pair<K, V> removeByIndex(int index) {
        return pairs.remove(index);
    }
    public boolean containsAll(Collection<?> c) {
        for (Object o : c)
            if (!contains(o))
                return false;
        return true;
    }

    public boolean addAll(Collection<? extends Pair<K, V>> c) {
        for (Pair<K, V> pair : c)
            if (!add(pair))
                return false;
        return true;
    }
    public MultiMap<K,V> addAll(Map<K, V> map) {
        for (Entry<K, V> entry : map.entrySet())
            add(entry.getKey(), entry.getValue());
        return this;
    }
    public MultiMap<K,V> merge(MultiMap<K,V> map) {
        if (!addAll(map))
            throw new RuntimeException("Can't merge MapArray");
        return this;
    }

    public boolean removeAll(Collection<?> c) {
        for (Object o : c)
            if (!remove(o))
                return false;
        return true;
    }

    public boolean retainAll(Collection<?> c) {
        for (Pair pair : pairs)
            if (!c.contains(pair))
                if (!remove(pair))
                    return false;
        return true;
    }

    public void clear() {
        pairs.clear();
    }

    @Override
    public String toString() {
        return print(LinqUtils.select(pairs, pair -> pair.key + ":" + pair.value));
    }

    @Override
    public MultiMap<K, V> clone() {
        return new MultiMap<>(this);
    }

    public MultiMap<K, V> copy() {
        return clone();
    }

    public <T1> List<T1> select(JFunc2<K, V, T1> func) {
        try {
            List<T1> result = new ArrayList<>();
            for (Pair<K,V> pair : pairs)
                result.add(func.invoke(pair.key, pair.value));
            return result;
        } catch (Exception ignore) {
            throwRuntimeException(ignore);
            return new ArrayList<>();
        }
    }
    public <T1> List<T1> map(JFunc2<K, V, T1> func) {
        return select(func);
    }

    public MultiMap<K, V> filter(JFunc2<K, V, Boolean> func) {
        return where(func);
    }
    public MultiMap<K, V> where(JFunc2<K, V, Boolean> func) {
        try {
            MultiMap<K, V> result = new MultiMap<>();
            for (Pair<K,V> pair : pairs)
                if (func.invoke(pair.key, pair.value))
                    result.add(pair);
            return result;
        } catch (Exception ignore) {
            throwRuntimeException(ignore);
            return null;
        }
    }
    public void ifDo(JFunc1<Pair<K, V>, Boolean> condition, JAction1<V> action) {
        try {
            for (Pair<K,V> el : pairs)
                if (condition.invoke(el))
                    action.invoke(el.value);
        } catch (Exception ignore) {
            throwRuntimeException(ignore);
        }
    }
    public <T> List<T> ifSelect(JFunc2<K, V, Boolean> condition, JFunc1<V, T> transform) {
        try {
            List<T> result = new ArrayList<>();
            for (Pair<K,V> el : pairs)
                if (condition.invoke(el.key, el.value))
                    result.add(transform.invoke(el.value));
            return result;
        } catch (Exception ignore) {
            throwRuntimeException(ignore);
            return null;
        }
    }

    public V firstValue(JFunc2<K, V, Boolean> func) {
        Pair<K, V> first = first(func);
        return first == null ? null : first.value;
    }
    public Pair<K, V> first(JFunc2<K, V, Boolean> func) {
        try {
            for (Pair<K, V> pair : pairs)
                if (func.invoke(pair.key, pair.value))
                    return pair;
            return null;
        } catch (Exception ignore) {
            throwRuntimeException(ignore);
            return null;
        }
    }
    public V lastValue(JFunc2<K, V, Boolean> func) {
        Pair<K, V> last = last(func);
        return last == null ? null : last.value;
    }
    public Pair<K, V> last(JFunc2<K, V, Boolean> func) {
        Pair<K, V> result = null;
        try {
            for (Pair<K, V> pair : pairs)
                if (func.invoke(pair.key, pair.value))
                    result = pair;
            return result;
        } catch (Exception ignore) {
            throwRuntimeException(ignore);
            return null;
        }
    }

    public MultiMap<K,V> slice(int from, int to) {
        return new MultiMap<>(listCopy(pairs, from, to));
    }
    public MultiMap<K,V> slice(int from) {
        return new MultiMap<>(listCopy(pairs, from));
    }
    public MultiMap<K,V> sliceTo(int to) {
        return new MultiMap<>(listCopyUntil(pairs, to));
    }
    public void foreach(JAction2<K, V> action) {
        try {
            for (Pair<K, V> pair : pairs)
                action.invoke(pair.key, pair.value);
        } catch (Exception ignore) {
            throwRuntimeException(ignore);
        }
    }

    public <R> List<R> selectMany(JFunc2<K, V, List<R>> func) {
        try {
            List<R> result = new ArrayList<>();
            for (Pair<K, V> pair : pairs)
                result.addAll(func.invoke(pair.key, pair.value));
            return result;
        } catch (Exception ignore) {
            throwRuntimeException(ignore);
            return null;
        }
    }
}