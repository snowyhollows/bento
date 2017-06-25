package net.snowyhollows.bento2.store;

import java.util.HashMap;
import java.util.Map;

public class MapStore implements BentoStore {

    private final Map<String, Object> data;

    public MapStore() {
        this.data = new HashMap<>();
    }

    @Override
    public Object get(String key) {
        return this.data.get(key);
    }

    @Override
    public void put(String key, Object value) {
        this.data.put(key, value);
    }


}