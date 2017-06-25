package net.snowyhollows.bento2.store;

public interface BentoStore {
    Object get(String key);
    void put(String key, Object value);
}
