package net.snowyhollows.bento2;

import net.snowyhollows.bento2.store.MapStore;
import net.snowyhollows.bento2.store.BentoStore;

public final class Bento {
    private final Bento parent;
    private final BentoStore store;

    private Bento(Bento parent, BentoStore store) {
        this.parent = parent;
        this.store = store;
    }

    private Object retrieveObjectOrNull(String key) {
        Object result = store.get(key);
        if (result != null) {
            return result;
        }
        if (parent != null) {
            final Object retrievedFromParent = parent.retrieveObjectOrNull(key);
            if (retrievedFromParent != null) {
                return retrievedFromParent;
            }
        }
        return null;
    }

    private Object retrieveObjectOrFail(String key) {
        Object o = retrieveObjectOrNull(key);
        if (o == null) {
            throw new BentoException("Couldn't retrieve " + key);
        }
        return o;
    }

    public int getInt(String key) {
        return Integer.parseInt(retrieveObjectOrFail(key).toString());
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(retrieveObjectOrFail(key).toString());
    }

    public float getFloat(String key) {
        return Float.parseFloat(retrieveObjectOrFail(key).toString());
    }

    public String getString(String key) {
        return retrieveObjectOrFail(key).toString();
    }

    public<T extends Enum<T>> T getEnum(Class<T> clazz, String key) {
        return Enum.valueOf(clazz, getString(key));
    }

    public static Bento createRoot() {
        return new Bento(null, new MapStore());
    }

    public Bento create() {
        return new Bento(this, new MapStore());
    }

    public void register(String key, int anInt) {
        store.put(key, anInt);
    }

    public<T extends Enum> void register(String key, T value) {
        store.put(key, value);
    }

    public void register(String key, float value) {
        store.put(key, value);
    }

    public void register(String key, String value) {
        store.put(key, value);
    }

    public void registerFactory(String key, BentoFactory bentoFactory) {
        store.put(key, bentoFactory);
    }

    public<T> T registerObject(String key, T o) {
        store.put(key, o);
        return o;
    }

    public<T> T get(BentoFactory<T> bentoFactory) {
        final String key = bentoFactory.getClass().getName();
        final Object createdEarlier = store.get(key);
        if (createdEarlier != null) {
            return (T)createdEarlier;
        }
        final T object = bentoFactory.createInContext(this);
        store.put(key, object);
        return object;
    }

    public<T> T get(String key) {
        final Object result = retrieveObjectOrFail(key);
        if (result instanceof BentoFactory) {
            return (T) ((BentoFactory)result).createInContext(this);
        }
        return (T)result;
    }


}
