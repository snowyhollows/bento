package net.snowyhollows.bento.category;

import java.util.*;

import static java.util.Arrays.fill;

public class CategoryMap<K extends Category, V> implements Map<K, V> {

    private final Object[] values;
    private CategoryManager<K> manager;

    public CategoryMap(CategoryManager<K> manager) {
        this.values = new Object[manager.values().size()];
        this.manager = manager;
    }

    public CategoryMap(CategoryManager<K> manager, Map<K, V> map) {
        this(manager);
        putAll(map);
    }

    @Override
    public int size() {
        int size = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) size++;
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        for (int i = 0; i < values.length; i++) {
            if (value.equals(values[i])) return true;
        }
        return false;
    }

    @Override
    public V get(Object key) {
        return (V) values[((K)key).ordinal()];
    }

    @Override
    public V put(K key, V value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        V previous = (V) values[key.ordinal()];
        values[key.ordinal()] = value;
        return previous;
    }

    @Override
    public V remove(Object key) {
        Objects.requireNonNull(key);
        K k = (K) key;
        V previous = (V) values[k.ordinal()];
        values[k.ordinal()] = null;
        return previous;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        fill(values, null);
    }

    @Override
    public Set<K> keySet() {
        return new AbstractSet<K>() {
            @Override
            public Iterator<K> iterator() {
                return manager.values().stream().filter(key -> get(key) != null).iterator();
            }

            @Override
            public int size() {
                return CategoryMap.this.size();
            }
        };
    }

    @Override
    public Collection<V> values() {
        return new AbstractCollection<V>() {
            @Override
            public Iterator<V> iterator() {
                return manager.values().stream().map(key -> get(key)).filter(value -> value != null).iterator();
            }

            @Override
            public int size() {
                return CategoryMap.this.size();
            }
        };
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                return manager.values().stream()
                        .filter(key -> get(key) != null).map(key -> (Entry<K, V>)new AbstractMap.SimpleEntry<>(key, get(key))).iterator();
            }

            @Override
            public int size() {
                return CategoryMap.this.size();
            }
        };
    }

    public CategoryManager<K> getManager() {
        return manager;
    }
}
