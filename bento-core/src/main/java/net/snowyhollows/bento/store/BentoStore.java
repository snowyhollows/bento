package net.snowyhollows.bento.store;

public interface BentoStore {
    Object get(Object key);
    void put(Object key, Object value);
}
