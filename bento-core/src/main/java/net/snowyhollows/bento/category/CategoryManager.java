package net.snowyhollows.bento.category;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.BentoFactory;

import java.util.*;
import java.util.stream.Collectors;

public abstract class CategoryManager<T extends Category> {
    private final List<T> instances;
    private final Map<String, T> instancesMap = new HashMap<>(32);

    public CategoryManager(Bento bento, String configurationPrefix, BentoFactory<T> tBentoFactory) {
        this(bento, configurationPrefix, tBentoFactory, null);
    }


    public CategoryManager(Bento bento, String configurationPrefix, BentoFactory<T> tBentoFactory, String instancesPrefix) {
        if(configurationPrefix == null){
            configurationPrefix = this.getClass().getCanonicalName();
        }
        List<T> instancesList = new ArrayList<>();
        instances = Collections.unmodifiableList(instancesList);

        String[] instanceNames = null;

        if (instancesPrefix == null) {
            String[] tryWithoutPrefix = getInstanceNames(bento, configurationPrefix);
            instanceNames = tryWithoutPrefix.length > 0 ? tryWithoutPrefix : getInstanceNames(bento, configurationPrefix + "._");
        } else {
            instanceNames = getInstanceNames(bento, configurationPrefix + "." + instancesPrefix);
        }

        List<T> builtIns = getBuiltIns();

        for (T builtIn : builtIns) {
            instancesMap.put(builtIn.name(), builtIn);
            instancesList.add(builtIn);
            if (builtIn.ordinal() != instancesList.size() - 1) {
                throw new IllegalStateException("Built-in " + builtIn.name() + " has ordinal " + builtIn.ordinal() + " but it should be " + (instancesList.size() - 1));
            }
        }

        for (int i = 0; i < instanceNames.length; i++) {
            instanceNames[i] = instanceNames[i].trim();
            int ordinal = instancesList.size();
            Bento newbento = bento.createWithPrefix(configurationPrefix + "." + instanceNames[i] + ".");
            newbento.register("name", instanceNames[i]);
            newbento.register("ordinal", ordinal);
            T obj = newbento.get(tBentoFactory);
            instancesList.add(obj);
            instancesMap.put(instanceNames[i], obj);
        }
    }

    protected List<T> getBuiltIns() {
        return Collections.emptyList();
    }

    private String[] getInstanceNames(Bento bento, String configurationPrefix) {
        if (exists(bento, configurationPrefix)) {
            return Arrays.asList(trimBrackets(bento.getString(configurationPrefix)).trim().split(","))
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

    private String trimBrackets(String string) {
        if (string.startsWith("[") && string.endsWith("]")) {
            return string.substring(1, string.length() - 1);
        }
        return string;
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
