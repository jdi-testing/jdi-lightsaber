package com.epam.jdi.tools.map;

/**
 * Created by Roman Iovlev on 14.02.2018
 * Email: roman.iovlev.jdi@gmail.com; Skype: roman.iovlev
 */

import com.epam.jdi.tools.LinqUtils;
import com.epam.jdi.tools.func.JAction1;
import com.epam.jdi.tools.func.JAction2;
import com.epam.jdi.tools.func.JFunc2;
import com.epam.jdi.tools.pairs.Pair;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import static com.epam.jdi.tools.LinqUtils.*;
import static com.epam.jdi.tools.PrintUtils.print;
import static com.epam.jdi.tools.TryCatchUtil.throwRuntimeException;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public class MultiMap<K, V> implements Collection<Pair<K, V>>, Cloneable {
    private List<Pair<K, V>> pairs;
    public List<Pair<K, V>> getPairs() {
        return pairs;
    }

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
            throw cantCreateException(ex);
        }
    }

    public MultiMap(Collection<K> collection, Function<K, V> value) {
        this(collection, k -> k, value);
    }
    public <T> MultiMap(Collection<T> collection, Function<T, K> keyFunc, Function<T, V> valueFunc) {
        this();
        try {
            for (T t : collection)
                add(keyFunc.apply(t), valueFunc.apply(t));
        } catch (Exception ex) {
            throw cantCreateException(ex);
        }
    }

    public MultiMap(K[] array, Function<K, V> value) {
        this(asList(array), value);
    }
    public <T> MultiMap(T[] array, Function<T, K> key, Function<T, V> value) {
        this(asList(array), key, value);
    }

    public MultiMap(int count, Function<Integer, K> keyFunc, Function<Integer, V> value) {
        this();
        try {
            for (int i = 0; i < count; i++)
                add(keyFunc.apply(i), value.apply(i));
        } catch (Exception ex) {
            throw cantCreateException(ex);
        }
    }
    public MultiMap(int count, Function<Integer, Pair<K, V>> pairFunc) {
        this();
        try {
            for (int i = 0; i < count; i++) {
                Pair<K, V> pair = pairFunc.apply(i);
                add(pair.key, pair.value);
            }
        } catch (Exception ex) {
            throw cantCreateException(ex);
        }
    }

    public MultiMap(MultiMap<K, V> mapArray) {
        this();
        this.pairs = mapArray.pairs;
        this.ignoreKeyCase = mapArray.ignoreKeyCase;
    }
    public MultiMap(MapArray<K, V> mapArray) {
        this();
        this.pairs = mapArray.pairs;
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
        if (keys == null || values == null) 
            throw new RuntimeException("Can't create MultiMap for null keys or values");
        if (keys.size() != values.size())
            throw new RuntimeException(format("keys and values has different count (keys:[%s]; values:[%s])",
                safePrintCollection(keys), safePrintCollection(values)));
        for (int i = 0; i < keys.size(); i++)
            add(keys.get(i), values.get(i));
    }
    public MultiMap(K[] keys, V[] values) {
        this(asList(keys), asList(values));
    }
    
    private RuntimeException cantCreateException(Exception ex) {
        return new RuntimeException("Can't create MapArray", ex);
    }
    private RuntimeException cantConvertException(Exception ex) {
        throw new RuntimeException(format("Can't convert toMap. Exception: %s", ex.getMessage()));
    }

    public static <T> MultiMap<Integer, T> toMultiMap(Collection<T> collection) {
        MultiMap<Integer, T> multiMap = new MultiMap<>();
        int i = 0;
        for (T t : collection)
            multiMap.add(i++, t);
        return multiMap;
    }

    public static <Value> MultiMap<Integer, Value> toMultiMap(int count, Function<Integer, Value> valueFunc) {
        MultiMap<Integer, Value> multiMap = new MultiMap<>();
        try {
            for (int i = 0; i < count; i++)
                multiMap.add(i, valueFunc.apply(i));
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
                result.add(key.apply(pair.key, pair.value), value.apply(pair.key, pair.value));
        } catch (Exception ex) {
            throw cantConvertException(ex);
        }
        return result;
    }

    public <VResult> MultiMap<K, VResult> toMultiMap(Function<V, VResult> value) {
        MultiMap<K, VResult> result = new MultiMap<>();
        result.ignoreKeyCase = ignoreKeyCase;
        try {
        for (Pair<K, V> pair : pairs)
            result.add(pair.key, value.apply(pair.value));
        return result;
        } catch (Exception ex) {
            throw cantConvertException(ex); }
    }

    public Map<K, V> toMap() {
        return toMap(v -> v);
    }
    public <VResult> Map<K, VResult> toMap(Function<V, VResult> value) {
        return toMap((k, v) -> k, (k,v) -> value.apply(v));
    }
    public <KResult, VResult> Map<KResult, VResult> toMap(
            JFunc2<K, V, KResult> key, JFunc2<K, V, VResult> value) {
        Map<KResult, VResult> result = new HashMap<>();
        try {
            for (Pair<K, V> pair : pairs)
                result.put(key.apply(pair.key, pair.value),
                        value.apply(pair.key, pair.value));
        } catch (Exception ex) {
            throw cantConvertException(ex);
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
    public List<V> values(Function<V, Boolean> condition) {
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
    public boolean any(Function<V, Boolean> func) {
        return LinqUtils.any(this.values(), func);
    }
    public boolean all(Function<V, Boolean> func) {
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
        return pairs.stream().filter(pair -> !c.contains(pair)).allMatch(this::remove);
    }

    public void clear() {
        pairs.clear();
    }

    @Override
    public String toString() {
        return print(pairs, pair -> pair.key + ":" + pair.value);
    }

    @Override
    public MultiMap<K, V> clone() {
        return new MultiMap<>(this);
    }

    public MultiMap<K, V> copy() {
        return clone();
    }

    public <T1> List<T1> map(JFunc2<K, V, T1> func) {
        return select(func);
    }
    public <T1> List<T1> map(Function<V, T1> func) {
        return select(func);
    }
    public <T1> List<T1> select(JFunc2<K, V, T1> func) {
        try {
            List<T1> result = new ArrayList<>();
            for (Pair<K,V> pair : pairs)
                result.add(func.apply(pair.key, pair.value));
            return result;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return new ArrayList<>();
        }
    }
    public <T1> List<T1> select(Function<V, T1> func) {
        try {
            List<T1> result = new ArrayList<>();
            for (Pair<K,V> pair : pairs)
                result.add(func.apply(pair.value));
            return result;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return new ArrayList<>();
        }
    }

    public MultiMap<K, V> filter(JFunc2<K, V, Boolean> func) {
        return where(func);
    }
    public MultiMap<K, V> filter(Function<V, Boolean> func) {
        return where(func);
    }
    public MultiMap<K, V> where(JFunc2<K, V, Boolean> func) {
        try {
            MultiMap<K, V> result = new MultiMap<>();
            for (Pair<K,V> pair : pairs)
                if (invokeBoolean(func, pair.key, pair.value))
                    result.add(pair);
            return result;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return null;
        }
    }
    public MultiMap<K, V> where(Function<V, Boolean> func) {
        try {
            MultiMap<K, V> result = new MultiMap<>();
            for (Pair<K,V> pair : pairs)
                if (invokeBoolean(func, pair.value))
                    result.add(pair);
            return result;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return null;
        }
    }
    public void ifDo(JFunc2<K, V, Boolean> condition, JAction1<V> action) {
        try {
            for (Pair<K,V> el : pairs)
                if (invokeBoolean(condition, el.key, el.value))
                    action.invoke(el.value);
        } catch (Exception ex) {
            throwRuntimeException(ex);
        }
    }
    public void ifDo(Function<V, Boolean> condition, JAction1<V> action) {
        try {
            for (Pair<K,V> el : pairs)
                if (invokeBoolean(condition, el.value))
                    action.invoke(el.value);
        } catch (Exception ex) {
            throwRuntimeException(ex);
        }
    }
    public <T> List<T> ifSelect(JFunc2<K, V, Boolean> condition, Function<V, T> transform) {
        try {
            List<T> result = new ArrayList<>();
            for (Pair<K,V> el : pairs)
                if (invokeBoolean(condition, el.key, el.value))
                    result.add(transform.apply(el.value));
            return result;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return null;
        }
    }
    public <T> List<T> ifSelect(Function<V, Boolean> condition, Function<V, T> transform) {
        try {
            List<T> result = new ArrayList<>();
            for (Pair<K,V> el : pairs)
                if (invokeBoolean(condition, el.value))
                    result.add(transform.apply(el.value));
            return result;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return null;
        }
    }

    public V firstValue(JFunc2<K, V, Boolean> func) {
        Pair<K, V> first = first(func);
        return first == null ? null : first.value;
    }
    public V firstValue(Function<V, Boolean> func) {
        Pair<K, V> first = first(func);
        return first == null ? null : first.value;
    }
    public Pair<K, V> first(JFunc2<K, V, Boolean> func) {
        try {
            for (Pair<K, V> pair : pairs)
                if (invokeBoolean(func, pair.key, pair.value))
                    return pair;
            return null;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return null;
        }
    }
    public Pair<K, V> first(Function<V, Boolean> func) {
        try {
            for (Pair<K, V> pair : pairs)
                if (invokeBoolean(func, pair.value))
                    return pair;
            return null;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return null;
        }
    }
    public V lastValue(JFunc2<K, V, Boolean> func) {
        Pair<K, V> last = last(func);
        return last == null ? null : last.value;
    }
    public V lastValue(Function<V, Boolean> func) {
        Pair<K, V> last = last(func);
        return last == null ? null : last.value;
    }
    public Pair<K, V> last(JFunc2<K, V, Boolean> func) {
        Pair<K, V> result = null;
        try {
            for (Pair<K, V> pair : pairs)
                if (invokeBoolean(func, pair.key, pair.value))
                    result = pair;
            return result;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return null;
        }
    }
    public Pair<K, V> last(Function<V, Boolean> func) {
        Pair<K, V> result = null;
        try {
            for (Pair<K, V> pair : pairs)
                if (invokeBoolean(func, pair.value))
                    result = pair;
            return result;
        } catch (Exception ex) {
            throwRuntimeException(ex);
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
        } catch (Exception ex) {
            throwRuntimeException(ex);
        }
    }
    public void foreach(JAction1<V> action) {
        try {
            for (Pair<K, V> pair : pairs)
                action.invoke(pair.value);
        } catch (Exception ex) {
            throwRuntimeException(ex);
        }
    }
    public <R> List<R> selectMany(JFunc2<K, V, List<R>> func) {
        try {
            List<R> result = new ArrayList<>();
            for (Pair<K, V> pair : pairs)
                result.addAll(func.apply(pair.key, pair.value));
            return result;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return null;
        }
    }
    public <R> List<R> selectMany(Function<V, List<R>> func) {
        try {
            List<R> result = new ArrayList<>();
            for (Pair<K, V> pair : pairs)
                result.addAll(func.apply(pair.value));
            return result;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return null;
        }
    }
}