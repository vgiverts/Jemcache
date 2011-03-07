package org.jemcache.util;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 7, 2007
 * Time: 11:48:51 AM
 */
public interface IOutput<T> {
    public void write(T message);
}
