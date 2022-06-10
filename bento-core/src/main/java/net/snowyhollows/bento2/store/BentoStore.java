package net.snowyhollows.bento2.store;

public interface BentoStore {
    Object get(Object key);
    void put(Object key, Object value);
}
