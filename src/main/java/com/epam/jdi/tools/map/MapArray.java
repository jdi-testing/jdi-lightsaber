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

import static com.epam.jdi.tools.PrintUtils.print;
import static com.epam.jdi.tools.TryCatchUtil.throwRuntimeException;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public class MapArray<K, V> implements Collection<Pair<K, V>>, Cloneable {
    public List<Pair<K, V>> pairs;

    public static <K, V> MapArray<K, V> map(Pair<K, V>... pairs) {
        return new MapArray<>(pairs);
    }
    public static <V> MapArray<Integer, V> map(List<V> list) {
        return new MapArray<>(list.size(), i -> i, list::get);
    }

    public MapArray() {
        pairs = new ArrayList<>();
    }

    public MapArray(K key, V value) {
        this();
        add(key, value);
    }
    public MapArray(Pair<K, V>... pairs) {
        this();
        try {
            for (Pair<K, V> pair : pairs)
                add(pair.key, pair.value);
        } catch (Exception ex) {
            throw new RuntimeException(format("Can't create MapArray. Exception: %s", ex.getMessage()));
        }
    }

    public MapArray(Collection<K> collection, JFunc1<K, V> value) {
        this(collection, k -> k, value::execute);
    }
    public <T> MapArray(Collection<T> collection, JFunc1<T, K> keyFunc, JFunc1<T, V> valueFunc) {
        this();
        try {
            for (T t : collection)
                add(keyFunc.invoke(t), valueFunc.invoke(t));
        } catch (Exception ex) {
            throw new RuntimeException(format("Can't create MapArray. Exception: %s", ex.getMessage()));
        }
    }

    public MapArray(K[] array, JFunc1<K, V> value) {
        this(asList(array), value);
    }
    public <T> MapArray(T[] array, JFunc1<T, K> key, JFunc1<T, V> value) {
        this(asList(array), key, value);
    }

    public MapArray(int count, JFunc1<Integer, K> keyFunc, JFunc1<Integer, V> value) {
        this();
        try {
        for (int i = 0; i < count; i++)
            add(keyFunc.invoke(i), value.invoke(i));
        } catch (Exception ex) {
            throw new RuntimeException(format("Can't create MapArray. Exception: %s", ex.getMessage()));
        }
    }
    public MapArray(int count, JFunc1<Integer, Pair<K, V>> pairFunc) {
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
    public void addUnique(K key, V value) {
        if (keys().contains(key))
            throw new RuntimeException("Duplicated keys " + key + ". Can't create MapArray");
        add(key, value);
    }

    public MapArray(MapArray<K, V> mapArray) {
        this();
        addAll(new ArrayList<>(mapArray));
    }
    public MapArray(Map<K, V> map) {
        this();
        for (Entry<K, V> entry : map.entrySet())
            add(entry.getKey(), entry.getValue());
    }

    public MapArray(Object[][] objects) {
        this();
        add(objects);
    }
    public MapArray(List<K> keys, List<V> values) {
        this();
        assert keys != null;
        if (values == null || keys.size() != values.size())
            throw new RuntimeException("keys and values null or has not equal count ");
        for (int i = 0; i < keys.size(); i++)
            add(keys.get(i), values.get(i));
    }
    public MapArray(K[] keys, V[] values) {
        this(asList(keys), asList(values));
    }

    public static <T> MapArray<Integer, T> toMapArray(Collection<T> collection) {
        MapArray<Integer, T> mapArray = new MapArray<>();
        int i = 0;
        for (T t : collection)
            mapArray.add(i++, t);
        return mapArray;
    }

    public static <Value> MapArray<Integer, Value> toMapArray(int count, JFunc1<Integer, Value> valueFunc) {
        MapArray<Integer, Value> mapArray = new MapArray<>();
        try {
            for (int i = 0; i < count; i++)
                mapArray.add(i, valueFunc.invoke(i));
        } catch (Exception ex) {
            throw new RuntimeException(format("Can't get MapArray. Exception: %s", ex.getMessage())); }
        return mapArray;
    }
    public static <T> MapArray<Integer, T> toMapArray(T[] array) {
        return toMapArray(asList(array));
    }

    public static <Key, Value> MapArray<Key, Value> toMapArray(Map<Key, Value> map) {
        MapArray<Key, Value> mapArray = new MapArray<>();
        for (Entry<Key, Value> e : map.entrySet())
            mapArray.add(e.getKey(), e.getValue());
        return mapArray;
    }

    public <KResult, VResult> MapArray<KResult, VResult> toMapArray(
            JFunc2<K, V, KResult> key, JFunc2<K, V, VResult> value) {
        MapArray<KResult, VResult> result = new MapArray<>();
        try {
            for (Pair<K, V> pair : pairs)
                result.add(key.invoke(pair.key, pair.value), value.invoke(pair.key, pair.value));
        } catch (Exception ex) {
            throw new RuntimeException(format("Can't convert toMap. Exception: %s", ex.getMessage())); }
        return result;
    }

    public <VResult> MapArray<K, VResult> toMapArray(JFunc1<V, VResult> value) {
        MapArray<K, VResult> result = new MapArray<>();
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

    public static boolean IGNORE_NOT_UNIQUE = false;

    public MapArray<K, V> add(K key, V value) {
        if (has(key)) {
            if (!IGNORE_NOT_UNIQUE)
                throw new RuntimeException("Key "+ key +" already exist");
        }
        else
            pairs.add(new Pair<>(key, value));
        return this;
    }
    public MapArray<K, V> update(K key, V value) {
        if (has(key))
            removeByKey(key);
        add(key, value);
        return this;
    }

    public MapArray<K, V> update(K key, JFunc1<V, V> func) {
        V value = null;
        if (has(key)) {
            value = get(key);
            removeByKey(key);
        }
        try {
            pairs.add(new Pair<>(key, func.invoke(value)));
        } catch (Exception ex) {
            throw new RuntimeException(format("Can't do update. Exception: %s", ex.getMessage()));
        }
        return this;
    }

    public void add(Object[][] pairs) {
        for (Object[] pair : pairs)
            if (pair.length == 2)
                add(cast(pair[0]), cast(pair[1]));
    }

    public void update(Object[][] pairs) {
        for (Object[] pair : pairs)
            if (pair.length == 2)
                update(cast(pair[0]), (V)cast(pair[1]));
    }

    private <R> R cast(Object obj) {
        try {
            return (R) obj;
        } catch (ClassCastException ex) {
            throw new ClassCastException(format("Can't cast element '%s' in MapArray. Exception: %s", obj, ex.getMessage()));
        }
    }

    public void addOrReplace(K key, V value) {
        if (has(key))
            removeByKey(key);
        add(key, value);
    }

    public void addOrReplace(Object[][] pairs) {
        for (Object[] pair : pairs)
            if (pair.length == 2)
                addOrReplace((K) pair[0], (V) pair[1]);
    }

    public boolean has(K key) {
        return keys().contains(key);
    }

    public MapArray<K, V> addFirst(K key, V value) {
        if (has(key))
            throw new RuntimeException(format(
                "Can't addFirst element for key '%s'. MapArray already element with this key.", key));
        List<Pair<K, V>> result = new CopyOnWriteArrayList<>();
        result.add(new Pair<>(key, value));
        result.addAll(pairs);
        pairs = result;
        return this;
    }

    public V get(K key) {
        Pair<K, V> first = null;
        try {
            first = LinqUtils.first(pairs, pair -> pair.key.equals(key));
        } catch (Exception ignore) {
        }
        return (first != null) ? first.value : null;
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

    public List<V> values() {
        return LinqUtils.map(pairs, pair -> pair.value);
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

    public boolean any() {
        return size() > 0;
    }
    public boolean any(JFunc1<V, Boolean> func) {
        return LinqUtils.any(this.values(), func);
    }

    public Pair<K, V> first() {
        return get(0);
    }

    public Pair<K, V> last() {
        return get(-1);
    }

    public MapArray<K, V> revert() {
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
        pairs.remove(
            LinqUtils.firstIndex(pairs, pair -> pair.key.equals(key)));
    }

    public void removeAllValues(V value) {
        pairs.removeAll(LinqUtils.where(pairs,
                p -> p.value.equals(value)));
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
    public MapArray<K,V> addAll(Map<K, V> map) {
        for (Entry<K, V> entry : map.entrySet())
            add(entry.getKey(), entry.getValue());
        return this;
    }
    public MapArray<K,V> merge(MapArray<K,V> map) {
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
    public MapArray<K, V> clone() {
        return new MapArray<>(this);
    }

    public MapArray<K, V> copy() {
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

    public MapArray<K, V> filter(JFunc2<K, V, Boolean> func) {
        return where(func);
    }
    public MapArray<K, V> where(JFunc2<K, V, Boolean> func) {
        try {
            MapArray<K, V> result = new MapArray<>();
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