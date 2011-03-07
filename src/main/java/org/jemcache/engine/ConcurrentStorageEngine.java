package org.jemcache.engine;

import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 7, 2007
 * Time: 3:48:08 PM
 */
public class ConcurrentStorageEngine implements IStorageEngine {

    private final ConcurrentMap<String, IDataContainer> cache = new ConcurrentHashMap<String, IDataContainer>(1024 * 1024);

    public boolean replace(String key, IDataContainer newContainer) {
        IDataContainer oldContainer = cache.get(key);
        if (oldContainer != null) {
            cache.put(key, newContainer);
            return true;
        }
        return false;
    }

    public boolean putIfAbsent(String key, IDataContainer container) {
        return cache.putIfAbsent(key, container) == null;
    }

    public IDataContainer get(String key) {
        return cache.get(key);
    }

    public boolean replace(String key, IDataContainer oldContainer, IDataContainer newContainer) {
        return cache.replace(key, oldContainer, newContainer);
    }


    public void put(String key, IDataContainer container) {
        cache.put(key, container);
    }


    public boolean remove(String key) {
        return cache.remove(key) != null;
    }
}
