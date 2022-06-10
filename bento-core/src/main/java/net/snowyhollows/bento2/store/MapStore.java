package net.snowyhollows.bento2.store;

import java.util.HashMap;
import java.util.Map;

public class MapStore implements BentoStore {

    private final Map<Object, Object> data;

    public MapStore() {
        this.data = new HashMap<Object, Object>();
    }

    @Override
    public Object get(Object key) {
        return this.data.get(key);
    }

    @Override
    public void put(Object key, Object value) {
        this.data.put(key, value);
    }


}