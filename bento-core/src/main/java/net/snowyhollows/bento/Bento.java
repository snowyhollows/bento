package net.snowyhollows.bento;

import net.snowyhollows.bento.inspector.BentoInspector;
import net.snowyhollows.bento.store.BentoStore;
import net.snowyhollows.bento.store.MapStore;

public final class Bento {
    private final Bento parent;
    private final BentoStore store;

    private final String prefix;

    public static BentoInspector inspector = new BentoInspector() {
        @Override
        public void createChild(Bento parent, Bento child) {
            // noop
        }

        @Override
        public void createObject(Bento creator, Object key, Object value) {
            // noop
        }

        @Override
        public void disposeOfChild(Bento bento) {
            // noop
        }
    };

    private Bento(Bento parent, BentoStore store, String prefix) {
        this.parent = parent;
        this.store = store;
        this.prefix = prefix;
    }

    private Bento(Bento parent, BentoStore store) {
        this(parent, store, "");
    }

    private Object retrieveObjectOrNull(Object key) {
        if(key instanceof String) {
            key = prefix + key;
        }
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

    private Object retrieveObjectOrFail(Object key) {
        Object o = retrieveObjectOrNull(key);
        if (o == null) {
            throw new BentoException("Couldn't retrieve " + key);
        }
        return o;
    }

    public int getInt(Object key) {
        return Integer.parseInt(getString(key));
    }

    public boolean getBoolean(Object key) {
        return Boolean.parseBoolean(getString(key));
    }

    public float getFloat(Object key) {
        return Float.parseFloat(getString(key));
    }

    public String getString(Object key) {
        return retrieveObjectOrFail(key).toString();
    }

    public<T extends Enum<T>> T getEnum(Class<T> clazz, Object key) {
        return Enum.valueOf(clazz, getString(key));
    }

    public static Bento createRoot() {
        Bento root = new Bento(null, new MapStore());
        inspector.createChild(null, root);
        return root;
    }

    public static Bento run(BentoFactory<?> factory) {
        final Bento root = createRoot();
        root.get(factory);
        return root;
    }

    public Bento create() {
        return createWithPrefix("");
    }

    public Bento createWithPrefix(String prefix) {
        Bento bento = new Bento(this, new MapStore(), this.prefix + prefix);
        inspector.createChild(this, bento);
        return bento;
    }

    public void dispose() {
        inspector.disposeOfChild(this);
    }

    public void register(Object key, Object value) {
        if (key instanceof String) {
            key = prefix + key;
        }
        store.put(key, value);
        inspector.createObject(this, key, value);
    }

    public<T> T get(BentoFactory<T> factoryAsKey, T ifAbsent) {
        return get((Object)factoryAsKey, ifAbsent);
    }

    public<T> T get(Object key) {
        return get(key, null);
    }

    public<T> T get(BentoFactory<T> factoryAsKey) {
        return get((Object)factoryAsKey, null);
    }

    public<T> T get(Object key, Object ifAbsent) {
        final Object createdEarlier = retrieveObjectOrNull(key);
        if (createdEarlier != null) {
            if (createdEarlier instanceof BentoFactory) {
                return (T) ((BentoFactory)createdEarlier).createInContext(this);
            }
            return (T)createdEarlier;
        } else if (key instanceof BentoFactory) {
            final BentoFactory<T> factoryAsKey = (BentoFactory<T>)key;
            final T object = factoryAsKey.createInContext(this);
            register(factoryAsKey, object);
            return object;
        } else if (ifAbsent != null) {
            return (T)ifAbsent;
        } else {
                throw new BentoException("key [" + key + "] is absent and is not a BentoFactory instance");
        }
    }
}
