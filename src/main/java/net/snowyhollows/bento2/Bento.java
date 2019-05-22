package net.snowyhollows.bento2;

import net.snowyhollows.bento2.inspector.BentoInspector;
import net.snowyhollows.bento2.store.BentoStore;
import net.snowyhollows.bento2.store.MapStore;

public final class Bento {
    private final Bento parent;
    private final BentoStore store;

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

    private Bento(Bento parent, BentoStore store) {
        this.parent = parent;
        this.store = store;
    }

    private Object retrieveObjectOrNull(Object key) {
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
        Bento bento = new Bento(this, new MapStore());
        inspector.createChild(this, bento);
        return bento;
    }

    public void dispose() {
        inspector.disposeOfChild(this);
    }

    public void register(Object key, Object value) {
        store.put(key, value);
        inspector.createObject(this, key, value);
    }

    public<T> T get(BentoFactory<T> factoryAsKey) {
        return get((Object)factoryAsKey);
    }

    public<T> T get(Object key) {
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
        } else {
            throw new BentoException("key [" + key + "] is absent and is not a BentoFactory instance");
        }
    }
}
