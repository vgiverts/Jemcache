package org.jemcache.server;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 7, 2007
 * Time: 11:53:22 AM
 */
public interface ISocketSession {
    public Object setAttribute(String key, Object value);
    public Object getAttribute(String key);
    public Object removeAttribute(String key);
}
