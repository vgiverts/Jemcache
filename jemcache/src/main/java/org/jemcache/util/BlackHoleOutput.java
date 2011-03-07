package org.jemcache.util;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Jan 3, 2011
 * Time: 4:49:02 PM
 */
public class BlackHoleOutput<T> implements IOutput<T> {
    public void write(T message) {
        // Swallow the message
    }
}
