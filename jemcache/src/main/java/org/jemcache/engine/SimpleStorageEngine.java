package org.jemcache.engine;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 7, 2007
 * Time: 3:52:03 PM
 */
public class SimpleStorageEngine implements IStorageEngine {

    private final HashMap<String, IDataContainer> cache = new HashMap<String, IDataContainer>(1024 * 1024);

    public synchronized boolean replace(String key, IDataContainer newContainer) {
        if (cache.containsKey(key)) {
            cache.put(key, newContainer);
            return true;
        }
        return false;
    }

    public synchronized boolean putIfAbsent(String key, IDataContainer container) {
        IDataContainer oldVal = cache.get(key);
        if (oldVal == null) {
            cache.put(key, container);
            return true;
        } else {
            return false;
        }
    }

    public IDataContainer get(String key) {
        return cache.get(key);
    }

    public synchronized boolean replace(String key, IDataContainer oldContainer, IDataContainer newContainer) {
        IDataContainer myOldContainer = cache.get(key);
        if (myOldContainer != null && myOldContainer.equals(oldContainer)) {
            cache.put(key, newContainer);
            return true;
        }
        return false;
    }

    public synchronized void put(String key, IDataContainer container) {
        cache.put(key, container);
    }

    public synchronized boolean remove(String key) {
        return cache.remove(key) != null;
    }
}
