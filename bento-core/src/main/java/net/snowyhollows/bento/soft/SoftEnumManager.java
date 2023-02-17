package net.snowyhollows.bento.soft;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.BentoFactory;

import java.util.*;

public abstract class SoftEnumManager<T extends SoftEnum> {
    private final Bento bento;
    private final List<T> instances;
    private final Map<String, T> instancesMap = new HashMap<>(127);

    public SoftEnumManager(Bento bento, String configurationPrefix, BentoFactory<T> tBentoFactory) {
        this.bento = bento;
        if(configurationPrefix == null){
            configurationPrefix = this.getClass().getCanonicalName();
        }
        List<T> instancesList = new ArrayList<>();
        instances = Collections.unmodifiableList(instancesList);
        String[] instances = bento.getString(configurationPrefix).split(",");
        for (int i = 0; i < instances.length; i++) {
            instances[i] = instances[i].trim();
            Bento newbento = bento.createWithPrefix(configurationPrefix + "." + instances[i] + ".");
            newbento.register("name", instances[i]);
            newbento.register("ordinal", i);
            T obj = newbento.get(tBentoFactory);
            instancesList.add(obj);
            instancesMap.put(instances[i], obj);
        }

    }
    public List<T> values() {
        return instances;
    }

    public T getByName(String name) {
        T value = instancesMap.get(name);
        if (value == null) {
            throw new IllegalArgumentException("Value [" + name + "] is undefined");
        }
        return value;
    }

    public T getByOrdinal(int o) {
        return instances.get(o);
    }
}
