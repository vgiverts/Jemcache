package org.jemcache.engine;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: 1/4/11
 * Time: 2:24 PM
 */
public interface IStorageEngine {

    IDataContainer get(String key);

    boolean remove(String key);

    void put(String key, IDataContainer container);

    boolean putIfAbsent(String key, IDataContainer container);

    boolean replace(String key, IDataContainer newContainer);

    boolean replace(String key, IDataContainer oldContainer, IDataContainer newContainer);
}