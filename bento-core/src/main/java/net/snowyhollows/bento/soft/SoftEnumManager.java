package net.snowyhollows.bento.soft;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.BentoFactory;

import java.util.*;
import java.util.stream.Collectors;

public abstract class SoftEnumManager<T extends SoftEnum> {
    private final List<T> instances;
    private final Map<String, T> instancesMap = new HashMap<>(32);

    public SoftEnumManager(Bento bento, String configurationPrefix, BentoFactory<T> tBentoFactory) {
        if(configurationPrefix == null){
            configurationPrefix = this.getClass().getCanonicalName();
        }
        List<T> instancesList = new ArrayList<>();
        instances = Collections.unmodifiableList(instancesList);

        String[] instanceNames = getInstanceNames(bento, configurationPrefix);
        for (int i = 0; i < instanceNames.length; i++) {
            instanceNames[i] = instanceNames[i].trim();
            Bento newbento = bento.createWithPrefix(configurationPrefix + "." + instanceNames[i] + ".");
            newbento.register("name", instanceNames[i]);
            newbento.register("ordinal", i);
            T obj = newbento.get(tBentoFactory);
            instancesList.add(obj);
            instancesMap.put(instanceNames[i], obj);
        }
    }

    private String[] getInstanceNames(Bento bento, String configurationPrefix) {
        if (exists(bento, configurationPrefix)) {
            return Arrays.asList(bento.getString(configurationPrefix).trim().split(","))
                    .stream().map(String::trim).collect(Collectors.toList()).toArray(new String[0]);
        }

        List<String> result = new ArrayList<>();
        int i = 0;
        while (exists(bento, configurationPrefix + "." + i)) {
            result.add(bento.getString(configurationPrefix + "." + i).trim());
            i++;
        }
        return result.toArray(new String[0]);
    }

    private boolean exists(Bento bento, String key) {
        return bento.get(key, this) != this;
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

    public abstract T[] emptyArray();
}
