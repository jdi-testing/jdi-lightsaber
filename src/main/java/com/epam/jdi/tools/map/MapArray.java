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
import org.apache.commons.lang3.ObjectUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import static com.epam.jdi.tools.LinqUtils.*;
import static com.epam.jdi.tools.PrintUtils.print;
import static com.epam.jdi.tools.TryCatchUtil.throwRuntimeException;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class MapArray<K, V> implements Collection<Pair<K, V>>, Cloneable {
    public List<Pair<K, V>> pairs;

    @SafeVarargs
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
    @SafeVarargs
    public MapArray(Pair<K, V>... pairs) {
        this(asList(pairs));
    }
    public MapArray(List<Pair<K, V>> pairs) {
        this();
        try {
            for (Pair<K, V> pair : pairs)
                add(pair.key, pair.value);
        } catch (Exception ex) {
            throw cantCreateMapException(ex);
        }
    }
    private RuntimeException cantCreateMapException(Exception ex) {
        return new RuntimeException(format("Can't create MapArray. Exception: %s", ex.getMessage()));
    }
    private RuntimeException cantConvertMapException(Exception ex) {
        return new RuntimeException(format("Can't convert toMap. Exception: %s", ex.getMessage()));
    }

    public MapArray(Collection<K> collection, Function<K, V> value) {
        this(collection, k -> k, value);
    }
    public <T> MapArray(Collection<T> collection, Function<T, K> keyFunc, Function<T, V> valueFunc) {
        this(collection, keyFunc, valueFunc, false);
    }
    public <T> MapArray(Collection<T> collection, Function<T, K> keyFunc, Function<T, V> valueFunc, boolean ignoreNotUnique) {
        this();
        try {
            for (T t : collection) {
                if (ignoreNotUnique) {
                    addNew(keyFunc.apply(t), valueFunc.apply(t));
                }
                else {
                    add(keyFunc.apply(t), valueFunc.apply(t));
                }
            }
        } catch (Exception ex) {
            throw cantCreateMapException(ex);
        }
    }

    public MapArray(K[] array, Function<K, V> value) {
        this(asList(array), value);
    }
    public <T> MapArray(T[] array, Function<T, K> key, Function<T, V> value) {
        this(array, key, value, false);
    }
    public <T> MapArray(T[] array, Function<T, K> key, Function<T, V> value, boolean ignoreNotUnique) {
        this(asList(array), key, value, ignoreNotUnique);
    }

    public MapArray(int count, Function<Integer, K> keyFunc, Function<Integer, V> value) {
        this();
        try {
            for (int i = 0; i < count; i++)
                add(keyFunc.apply(i), value.apply(i));
        } catch (Exception ex) {
            throw cantCreateMapException(ex);
        }
    }
    public MapArray(int count, Function<Integer, Pair<K, V>> pairFunc) {
        this();
        try {
            for (int i = 0; i < count; i++) {
                Pair<K, V> pair = pairFunc.apply(i);
                add(pair.key, pair.value);
            }
        } catch (Exception ex) {
            throw cantCreateMapException(ex);
        }
    }
    public void addUnique(K key, V value) {
        if (keys().contains(key)) {
            throw new RuntimeException("Duplicated keys " + key + ". Can't create MapArray");
        }
        add(key, value);
    }

    public MapArray(MapArray<K, V> mapArray) {
        this();
        addAll(mapArray.pairs);
        this.ignoreNotUnique = mapArray.ignoreNotUnique;
    }
    public MapArray(Map<K, V> map) {
        this();
        if (map == null) return;
        for (Entry<K, V> entry : map.entrySet())
            add(entry.getKey(), entry.getValue());
    }

    public MapArray(Object[][] objects) {
        this();
        if (objects == null) return;
        add(objects);
    }
    public MapArray(Collection<K> keys, Collection<V> values) {
        this();
        if (keys == null || values == null) return;
        if (keys.size() != values.size())
            throw new RuntimeException(format("keys and values has different count (keys:[%s]; values:[%s])",
                    safePrintCollection(keys), safePrintCollection(values)));
        Iterator<K> ik = keys.iterator();
        Iterator<V> vk = values.iterator();
        for (int i = 0; i < keys.size(); i++) {
            add(ik.next(), vk.next());
        }
    }
    public MapArray(K[] keys, V[] values) {
        this(asList(keys), asList(values));
    }

    public static <T> MapArray<Integer, T> toMapArray(Collection<T> collection) {
        MapArray<Integer, T> mapArray = new MapArray<>();
        if (collection == null) {
            return mapArray;
        }
        int i = 0;
        for (T t : collection) {
            mapArray.add(i++, t);
        }
        return mapArray;
    }

    public static <Value> MapArray<Integer, Value> toMapArray(int count, Function<Integer, Value> valueFunc) {
        MapArray<Integer, Value> mapArray = new MapArray<>();
        try {
            for (int i = 0; i < count; i++) {
                mapArray.add(i, valueFunc.apply(i));
            }
        } catch (Exception ex) {
            throw new RuntimeException(format("Can't get MapArray. Exception: %s", ex.getMessage())); }
        return mapArray;
    }
    public static <T> MapArray<Integer, T> toMapArray(T[] array) {
        return toMapArray(asList(array));
    }

    public static <Key, Value> MapArray<Key, Value> toMapArray(Map<Key, Value> map) {
        MapArray<Key, Value> mapArray = new MapArray<>();
        if (map == null) {
            return mapArray;
        }
        for (Entry<Key, Value> e : map.entrySet()) {
            mapArray.add(e.getKey(), e.getValue());
        }
        return mapArray;
    }

    public <KResult, VResult> MapArray<KResult, VResult> toMapArray(
            JFunc2<K, V, KResult> key, JFunc2<K, V, VResult> value) {
        MapArray<KResult, VResult> result = new MapArray<>();
        try {
            for (Pair<K, V> pair : pairs) {
                result.add(key.apply(pair.key, pair.value), value.apply(pair.key, pair.value));
            }
        } catch (Exception ex) {
            throw cantConvertMapException(ex);
        }
        return result;
    }

    public <VResult> MapArray<K, VResult> toMapArray(Function<V, VResult> value) {
        MapArray<K, VResult> result = new MapArray<>();
        try {
            for (Pair<K, V> pair : pairs) {
                result.add(pair.key, value.apply(pair.value));
            }
            return result;
        } catch (Exception ex) {
            throw cantConvertMapException(ex); }
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
            for (Pair<K, V> pair : pairs) {
                result.put(key.apply(pair.key, pair.value), value.apply(pair.key, pair.value));
            }
        } catch (Exception ex) {
            throw cantConvertMapException(ex);
        }
        return result;
    }

    private boolean ignoreNotUnique = false;

    public void ignoreNotUnique() {
        ignoreNotUnique = true;
    }
    
    public MapArray<K, V> add(K key, V value) {
        if (has(key)) {
            if (!ignoreNotUnique) {
                throw new RuntimeException("Key '" + key + "' already exist. " + toString());
            }
        }
        else
            pairs.add(new Pair<>(key, value));
        return this;
    }
    public MapArray<K, V> addNew(K key, V value) {
        if (!has(key)) {
            pairs.add(new Pair<>(key, value));
        }
        return this;
    }
    public MapArray<K, V> update(K key, V value) {
        try {
            add(key, value);
        } catch (Exception ex) {
            if (ex.getMessage().contains("already exist")) {
                updateByKey(key, value);
            }
            else {
                throw ex;
            }
        }
        return this;
    }

    public MapArray<K, V> update(K key, Function<V, V> func) {
        try {
            if (has(key)) {
                V value = get(key);
                updateByKey(key, func.apply(value));
            } else {
                add(key, func.apply(null));
            }
        } catch (Exception ex) {
            throw new RuntimeException(format("Can't do update. Exception: %s", ex.getMessage()));
        }
        return this;
    }

    public void add(Object[][] pairs) {
        if (pairs == null) return;
        for (Object[] pair : pairs)
            if (pair.length == 2)
                add(cast(pair[0]), cast(pair[1]));
    }

    public void update(Object[][] pairs) {
        if (pairs == null) return;
        for (Object[] pair : pairs) {
            if (pair.length == 2) {
                update(cast(pair[0]), (V) cast(pair[1]));
            }
        }
    }

    private <R> R cast(Object obj) {
        try {
            return (R) obj;
        } catch (ClassCastException ex) {
            throw new ClassCastException(format("Can't cast element '%s' in MapArray. Exception: %s", obj, ex.getMessage()));
        }
    }

    public void addOrReplace(K key, V value) {
        if (has(key)) {
            removeByKey(key);
        }
        add(key, value);
    }

    public void addOrReplace(Object[][] pairs) {
        if (pairs == null) return;
        for (Object[] pair : pairs) {
            if (pair.length == 2) {
                addOrReplace((K) pair[0], (V) pair[1]);
            }
        }
    }

    public boolean has(K key) {
        return keys().contains(key);
    }

    public MapArray<K, V> addFirst(K key, V value) {
        if (has(key)) {
            throw new RuntimeException(format(
                    "Can't addFirst element for key '%s'. MapArray already have element with this key.", key));
        }
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
        } catch (Exception ignore) { }
        return first != null ? first.value : null;
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

    public int indexOf(K key) {
        int index = 0;
        for (Pair<K, V> pair : pairs) {
            if (pair.key.equals(key)) {
                return index;
            }
            else {
                index++;
            }
        }
        return -1;
    }

    public List<K> keys() {
        return pairs.stream().map(pair -> pair.key).collect(toList());
    }

    public List<V> values() {
        return pairs.stream().map(pair -> pair.value).collect(toList());
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

    public MapArray<K, V> revert() {
        List<Pair<K, V>> result = new CopyOnWriteArrayList<>();
        for (int i = size() - 1; i >= 0; i--) {
            result.add(get(i));
        }
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
    public MapArray<K, V> update(Pair<K, V> kv) {
        if (kv == null) {
            return this;
        }
        return update(kv.key, kv.value);
    }

    public boolean remove(Object o) {
        boolean isRemoved = false;
        for (Object kv : pairs) {
            if (kv.equals(o)) {
                pairs.remove(kv);
                isRemoved = true;
            }
        }
        return isRemoved;
    }

    public void removeByKey(K key) {
        for (Pair<K, V> pair : pairs) {
            if (pair.key.equals(key)) {
                pairs.remove(pair);
                return;
            }
        }
    }
    private void updateByKey(K key, V value) {
        for (Pair<K, V> pair : pairs) {
            if (pair.key.equals(key)) {
                pair.value = value;
                return;
            }
        }
    }

    public int firstIndex(Function<V, Boolean> func) {
        if (pairs == null || pairs.size() == 0) {
            throw new RuntimeException("Can't get firstIndex. Collection is Null or empty");
        }
        try {
            for (int i = 0; i < size(); i++) {
                if (invokeBoolean(func, pairs.get(i).value)) {
                    return i;
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Can't get firstIndex." + ex.getMessage());
        }
        return -1;
    }
    public void removeAllValues(V value) {
        List<Pair<K,V>> values = LinqUtils.where(pairs, p -> p.value.equals(value));
        if (ObjectUtils.isNotEmpty(value)) {
            pairs.removeAll(values);
        }
    }
    public Pair<K, V> removeByIndex(int index) {
        return pairs.remove(index);
    }

    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    public boolean addAll(Collection<? extends Pair<K, V>> c) {
        if (c == null)
            return false;
        for (Pair<K, V> pair : c) {
            if (!add(pair)) {
                return false;
            }
        }
        return true;
    }
    public MapArray<K,V> addAll(Map<K, V> map) {
        if (map == null) {
            return this;
        }
        for (Entry<K, V> entry : map.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
        return this;
    }
    public MapArray<K,V> merge(MapArray<K,V> map) {
        if (map == null) {
            return this;
        }
        if (!addAll(map)) {
            throw new RuntimeException("Can't merge MapArray");
        }
        return this;
    }

    public boolean removeAll(Collection<?> c) {
        if (c == null) {
            return false;
        }
        for (Object o : c) {
            if (!remove(o)) {
                return false;
            }
        }
        return true;
    }

    public boolean retainAll(Collection<?> c) {
        if (c == null) {
            return false;
        }
        for (Pair<K, V> pair : pairs) {
            if (!c.contains(pair) && !remove(pair)) {
                return false;
            }
        }
        return true;
    }

    public void clear() {
        pairs.clear();
    }

    @Override
    public String toString() {
        return print(pairs, pair -> pair.key + ":" + pair.value);
    }

    @Override
    public MapArray<K, V> clone() {
        return new MapArray<>(this);
    }

    public MapArray<K, V> copy() {
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
            for (Pair<K,V> pair : pairs) {
                result.add(func.apply(pair.key, pair.value));
            }
            return result;
        } catch (Exception ignore) {
            throwRuntimeException(ignore);
            return new ArrayList<>();
        }
    }
    public <T1> List<T1> select(Function<V, T1> func) {
        try {
            List<T1> result = new ArrayList<>();
            for (Pair<K,V> pair : pairs) {
                result.add(func.apply(pair.value));
            }
            return result;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return new ArrayList<>();
        }
    }

    public MapArray<K, V> filter(JFunc2<K, V, Boolean> func) {
        return where(func);
    }
    public MapArray<K, V> filter(Function<V, Boolean> func) {
        return where(func);
    }
    public MapArray<K, V> where(JFunc2<K, V, Boolean> func) {
        try {
            MapArray<K, V> result = new MapArray<>();
            for (Pair<K,V> pair : pairs) {
                if (invokeBoolean(func, pair.key, pair.value)) {
                    result.add(pair);
                }
            }
            return result;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return null;
        }
    }
    public MapArray<K, V> where(Function<V, Boolean> func) {
        try {
            MapArray<K, V> result = new MapArray<>();
            for (Pair<K,V> pair : pairs) {
                if (invokeBoolean(func, pair.value)) {
                    result.add(pair);
                }
            }
            return result;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return null;
        }
    }
    public void ifDo(JFunc2<K, V, Boolean> condition, JAction1<V> action) {
        try {
            for (Pair<K,V> el : pairs) {
                if (invokeBoolean(condition, el.key, el.value)) {
                    action.invoke(el.value);
                }
            }
        } catch (Exception ex) {
            throwRuntimeException(ex);
        }
    }
    public void ifDo(Function<V, Boolean> condition, JAction1<V> action) {
        try {
            for (Pair<K,V> el : pairs) {
                if (invokeBoolean(condition, el.value)) {
                    action.invoke(el.value);
                }
            }
        } catch (Exception ex) {
            throwRuntimeException(ex);
        }
    }
    public <T> List<T> ifSelect(JFunc2<K, V, Boolean> condition, Function<V, T> transform) {
        try {
            List<T> result = new ArrayList<>();
            for (Pair<K,V> el : pairs) {
                if (invokeBoolean(condition, el.key, el.value)) {
                    result.add(transform.apply(el.value));
                }
            }
            return result;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return null;
        }
    }
    public <T> List<T> ifSelect(Function<V, Boolean> condition, Function<V, T> transform) {
        try {
            List<T> result = new ArrayList<>();
            for (Pair<K,V> el : pairs) {
                if (invokeBoolean(condition, el.value)) {
                    result.add(transform.apply(el.value));
                }
            }
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
            for (Pair<K, V> pair : pairs) {
                if (invokeBoolean(func, pair.key, pair.value)) {
                    return pair;
                }
            }
            return null;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return null;
        }
    }
    public Pair<K, V> first(Function<V, Boolean> func) {
        try {
            for (Pair<K, V> pair : pairs) {
                if (invokeBoolean(func, pair.value)) {
                    return pair;
                }
            }
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
            for (Pair<K, V> pair : pairs) {
                if (invokeBoolean(func, pair.key, pair.value)) {
                    result = pair;
                }
            }
            return result;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return null;
        }
    }
    public Pair<K, V> last(Function<V, Boolean> func) {
        Pair<K, V> result = null;
        try {
            for (Pair<K, V> pair : pairs) {
                if (invokeBoolean(func, pair.value)) {
                    result = pair;
                }
            }
            return result;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return null;
        }
    }

    public MapArray<K,V> slice(int from, int to) {
        return new MapArray<>(listCopy(pairs, from, to));
    }
    public void foreach(JAction2<K, V> action) {
        try {
            for (Pair<K, V> pair : pairs) {
                action.invoke(pair.key, pair.value);
            }
        } catch (Exception ex) {
            throwRuntimeException(ex);
        }
    }

    public <R> List<R> selectMany(JFunc2<K, V, List<R>> func) {
        try {
            List<R> result = new ArrayList<>();
            for (Pair<K, V> pair : pairs) {
                result.addAll(func.apply(pair.key, pair.value));
            }
            return result;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return null;
        }
    }
    public <R> List<R> selectMany(Function<V, List<R>> func) {
        try {
            List<R> result = new ArrayList<>();
            for (Pair<K, V> pair : pairs) {
                result.addAll(func.apply(pair.value));
            }
            return result;
        } catch (Exception ex) {
            throwRuntimeException(ex);
            return null;
        }
    }
}